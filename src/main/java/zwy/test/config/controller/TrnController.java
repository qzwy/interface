package zwy.test.config.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestParam;
import zwy.test.config.DateNum;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tf.tibp.ncc.keys.KeyUtil;
import tf.tibp.ncc.keys.KeysConst;

@RestController
public class TrnController {

  final static Logger logger = LoggerFactory.getLogger(TrnController.class);

  @Autowired
  private StringRedisTemplate stringRedisTemplate;

  @GetMapping("/trainState")
  public ResponseEntity<?> searchTranGroup(String lineId, String stationName, String trainNo,
      String direction, String driveState, @RequestParam  List<Integer> delayTime)
      throws UnsupportedEncodingException {
    String[] lineList = new String[]{"00", "01", "02", "03", "04", "05", "07"};
    logger.info(
        "params: lineId: {}, stationName: {}, trainNo: {}, direction: {}, driveState: {}",
        lineId, stationName, trainNo, direction, driveState);
    List<String> keys  = new ArrayList<>();
    if (lineId.isEmpty()||lineId.equals("00")) {
      for (String line : lineList){
        keys.add(KeyUtil.forLine(line).domain(KeysConst.DOMAIN_TRN).day(DateNum.getDayNum()).name(KeysConst.KEY_TRN_OPERA).build());
      }
    }else {
      keys.add(KeyUtil.forLine(lineId).domain(KeysConst.DOMAIN_TRN).day(DateNum.getDayNum()).name(KeysConst.KEY_TRN_OPERA).build());
    }

    List<JSONObject> list = new ArrayList<>();
    for (String key : Objects.requireNonNull(keys)) {
      Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
      for (Object i : entries.values()) {
        list.add(JSONObject.parseObject(i.toString()));
      }
    }

    //只要不符合条件就清除
    for (int j = 0; j < list.size(); j++) {
      boolean flag = true;
//      if (list.get(j).get("alm_flag").equals("0") && !delayTime.isEmpty()) {
//        flag = true;
//      }
      if (!stationName.isEmpty()) {
        stationName = URLDecoder.decode(stationName, "UTF-8");
        if (!list.get(j).get("sta_name").equals(stationName)) {
          flag = false;
        }
      }
      if (!trainNo.isEmpty()) {
        if (!list.get(j).get("train_no").equals(trainNo)) {
          flag = false;
        }
      }
      if (!direction.isEmpty()) {
        if (!list.get(j).get("direct").equals(direction)) {
          flag = false;
        }
      }

      if (!driveState.isEmpty()) {
        driveState = URLDecoder.decode(driveState, "UTF-8");
        //提前
        if(driveState.equals("提前")){
          if (!list.get(j).get("alm_flag").equals("-2")) flag = false;
        }
       //正点
        if(driveState.equals("正点")){
          if (!list.get(j).get("alm_flag").equals("0")) flag = false;
        }
        //晚点
        if(driveState.equals("晚点")){
          if (!(list.get(j).get("termi_flag").equals("1") && list.get(j).getInteger("alm_flag") > 0)) flag = false;
        }
        //延误
        if(driveState.equals("延误")){
          if (!(!list.get(j).get("termi_flag").equals("1") && list.get(j).getInteger("alm_flag") > 0)) flag = false;
        }
      }
      if (delayTime.size() != 0){
        int  x = 0;
        for (Integer m : delayTime){
          if (list.get(j).getInteger("alm_flag").equals(m)) {
            System.out.println("list = " + list.get(j));
            x = 1;
          }
        }
        if (x == 0) flag = false;
      }
      if (!flag) {
        list.get(j).clear();
      }
    }
    list.removeIf(JSONObject::isEmpty);
    return ResponseEntity.ok(list);
  }

}
