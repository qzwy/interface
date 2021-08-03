package zwy.test.config.controller;

import com.alibaba.fastjson.JSONObject;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AlarmController {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @PostMapping("/event")
  public ResponseEntity<?> searchEvent(@RequestBody JSONObject paramList) {
    String lineId = "%";
    String stationId = "%";
    System.out.println("System.currentTimeMillis() = " + System.currentTimeMillis());
    String level = paramList.get("level").toString();
    String line = paramList.get("line").toString();
    String station = paramList.get("station").toString();
    String sys = paramList.get("sys").toString();
    String type = paramList.get("type").toString();
    String startTime = paramList.get("startTime").toString();
    String endTime = paramList.get("endTime").toString();
    int page = Integer.parseInt(paramList.get("page").toString());
    int size = Integer.parseInt(paramList.get("size").toString());

    System.out.println("line = " + line);
    if (!line.isEmpty() && (!line.equals("线网"))) {
      //lineName -> lineId
      String lineSql = "select id from ncc_base.base_line bl where name_cn  = ?";
      lineId = jdbcTemplate.queryForObject(lineSql, String.class, line);
    }
    if (!station.isEmpty() && (!station.equals("全网") && !station.equals("全线"))){
      //stationName -> stationId
      String stationSql = "select id from ncc_base.base_station bs  where line_id like ? and name_cn  = ?";
      jdbcTemplate.queryForList(stationSql, lineId, station);
    }

    if (level.isEmpty()) {
      level = "%";
    }
    if (sys.isEmpty()) {
      sys = "%";
    }
    if (type.isEmpty()) {
      type = "%";
    }
    if (startTime.isEmpty()) {
      startTime = "2000-01-01";
    }
    if (endTime.isEmpty()) {
      endTime = "2099-12-31";
    }

    String contentSql = "select * from ncc_pmart.iscs_event ie where occur between ? and ? "
        + "and alarm_level like ? and line_id like ? and station_id like ? and sys_id like ? and alarm_type like ? "
        + "order by occur desc limit ? offset ?";

    String numberSql = "select count(sys_id) from ncc_pmart.iscs_event ie where occur between ? and ? "
        + "and alarm_level like ? and line_id like ? and station_id like ? and sys_id like ? and alarm_type like ? ";

    List<Map<String, Object>> events = jdbcTemplate
        .queryForList(contentSql, startTime, endTime, level, lineId, stationId, sys, type, size, page);

    Long total = jdbcTemplate
        .queryForObject(numberSql, Long.class, startTime, endTime, level, lineId, stationId, sys, type);
    JSONObject result  = new JSONObject();
    result.put("content", events);
    result.put("total", total);
    result.put("totalPage", (int)Math.ceil((double)(total/size)));
    result.put("currentPage", page+1);
    System.out.println("total = " + total);
    boolean haveNext = total-((page+1)*size) > 0;
    result.put("haveNext", haveNext);
    return ResponseEntity.ok(result);
  }
}
