package zwy.test.config.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonParser;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class InterfaceController {

  @Autowired
  private StringRedisTemplate stringRedisTemplate;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  Map<String, String> getLineName() {
    String sql = "select id  as code, name_cn as name from ncc_base.base_line bl ";
    Map<String, String> lineName = new HashMap<>();
    jdbcTemplate.query(sql, new RowMapper<Map<String, String>>() {
      @Override
      public Map<String, String> mapRow(ResultSet resultSet, int i) throws SQLException {
        Map<String, String> object = new HashMap<>();
        object.put(resultSet.getString("code"), resultSet.getString("name"));
        lineName.put(resultSet.getString("code"), resultSet.getString("name"));
        return object;
      }
    });
    return lineName;
  }

  List<String> getLineIdSort(){
    String sql = "select id as code from ncc_base.base_line bl order by code";
    List<String> lineIds = new ArrayList<>();
    jdbcTemplate.query(sql, new RowMapper<Map<String, String>>() {
      @Override
      public Map<String, String> mapRow(ResultSet resultSet, int i) throws SQLException {
        lineIds.add(resultSet.getString("code"));
        return null;
      }
    });
    return lineIds;
  }


  @GetMapping("/SystemStatus")
  public ResponseEntity<?> systemRunStatus() {
      String sql = "select interface_id  id, interface_status status from ncc_meta.t01_interface_status tis order by interface_id ";
      Map<String, String> map = new HashMap<>();
      jdbcTemplate.query(sql, new RowMapper<Map<String, String>>() {
        @Override
        public Map<String, String> mapRow(ResultSet resultSet, int i) throws SQLException {
          Map<String, String> object = new HashMap<>();
          object.put(resultSet.getString("id"), resultSet.getString("status"));
          map.put(resultSet.getString("id"), resultSet.getString("status"));
          return object;
        }
      });

    String content = "[\n"
        + "{\n"
        + "\"code\": \"01\",\n"
        + "\"name\": \"一号线\",\n"
        + "\"content\": {\n"
        + "\"sig\": \" "+ map.get("113") +" \",\n"
        + "\"lc/mlc\": \"" + map.get("103") + "\",\n"
        + "\"iscs\": \"" + map.get("108") + "\"\n"
        + "}\n"
        + "},\n"
        + "{\n"
        + "\"code\": \"02\",\n"
        + "\"name\": \"二号线\",\n"
        + "\"content\": {\n"
        + "\"sig\": \"" + map.get("114") + "\",\n"
        + "\"lc/mlc\": \"" + map.get("104") + "\",\n"
        + "\"iscs\": \"" + map.get("109") + "\"\n"
        + "}\n"
        + "},\n"
        + "{\n"
        + "\"code\": \"03\",\n"
        + "\"name\": \"三号线\",\n"
        + "\"content\": {\n"
        + "\"sig\": \"" + map.get("115") + "\",\n"
        + "\"lc/mlc\": \"" + map.get("105") + "\",\n"
        + "\"iscs\": \"" + map.get("110") + "\"\n"
        + "}\n"
        + "},\n"
        + "{\n"
        + "\"code\": \"04\",\n"
        + "\"name\": \"四号线\",\n"
        + "\"content\": {\n"
        + "\"sig\": \"" + map.get("116") + "\",\n"
        + "\"lc/mlc\": \"" + map.get("106") + "\",\n"
        + "\"iscs\": \"" + "" + map.get("111") + "" + "\"\n"
        + "}\n"
        + "},\n"
        + "{\n"
        + "\"code\": \"05\",\n"
        + "\"name\": \"五号线\",\n"
        + "\"content\": {\n"
        + "\"sig\": \"" + map.get("117") + "\",\n"
        + "\"lc/mlc\": \"" + map.get("107") + "\",\n"
        + "\"iscs\": \"" + map.get("112") + "\"\n"
        + "}\n"
        + "},\n"
        + "{\n"
        + "\"code\": \"ot\",\n"
        + "\"name\": \"other\",\n"
        + "\"content\": {\n"
        + "\"hawq\": \"" + map.get("205") + "\",\n"
        + "\"cchs\": \"" + map.get("101") + "\",\n"
        + "\"redis\": \"" + map.get("204") + "\"\n"
        + "}\n"
        + "}\n"
        + "]";

    return ResponseEntity.ok(JSON.parse(content));
  }

  @GetMapping("/status")
  public  ResponseEntity<?> communication(){
    Object parse = JSONObject.parse(
        "[{\"code\":\"01\",\"name\":\"一号线\",\"content\":{\"iscs\":\"正常\",\"sig\":\"正常\",\"lc/mlc\":\"正常\"}},{\"code\":\"02\",\"name\":\"二号线\",\"content\":{\"lc/mlc\":\"正常\",\"sig\":\"正常\",\"iscs\":\"正常\"}},{\"code\":\"03\",\"name\":\"三号线\",\"content\":{\"iscs\":\"正常\",\"sig\":\"正常\",\"lc/mlc\":\"正常\"}},{\"code\":\"04\",\"name\":\"四号线\",\"content\":{\"sig\":\"正常\",\"lc/mlc\":\"正常\",\"iscs\":\"正常\"}},{\"code\":\"05\",\"name\":\"五号线\",\"content\":{\"iscs\":\"不正常\",\"sig\":\"正常\",\"lc/mlc\":\"正常\"}},{\"code\":\"ot\",\"name\":\"other\",\"content\":{\"cchs\":\"正常\",\"hawq\":\"正常\",\"redis\":\"正常\"}}]");
    return ResponseEntity.ok(parse);

  }

}
