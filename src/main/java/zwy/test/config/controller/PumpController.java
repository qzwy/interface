package zwy.test.config.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tf.tibp.ncc.keys.KeyUtil;
import tf.tibp.ncc.keys.KeysConst;

@RestController
public class PumpController {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private StringRedisTemplate stringRedisTemplate;

  private List<String> getPump(){
    String sql = "select value from public.tf_appprofiles ta where name  = 'device.waterpump'";
    return jdbcTemplate.queryForList(sql, String.class);
  }


  @GetMapping("/pumps")
  public ResponseEntity<?> cacullatePump(){
    List<String> pump = getPump();
    String[] split = pump.get(0).split(",");
    List<String> keys = new ArrayList<>();
    for(String i : split){
      keys.add(KeyUtil.forNone().day().domain(KeysConst.DOMAIN_ISCS)
          .typeAndName(i, KeysConst.KEY_DEV_PROP).build());
    }
    //获取属性狂数据
    List<Object> objects = stringRedisTemplate.executePipelined(
        (RedisCallback<Object>) redisConnection -> {
          for (String key : keys) {
            redisConnection.hGetAll(key.getBytes());
          }
          return null;
        });
    Map<String, Object> relation =  new HashMap<>();
    List<Map<String, Boolean>> property = new ArrayList<>();
    for(int num = 0; num < split.length; num++){
      Map<String, Boolean> map  = new HashMap<>();
      JSONObject equipments = JSONObject.parseObject(JSON.toJSONString(objects.get(num)));
      equipments.values().forEach(i -> {
        JSONObject jsonObject = JSONObject.parseObject(i.toString());
        if(jsonObject.getString("value").length() == 1){
          map.put(jsonObject.getString("point").split("_")[2], changeToBool(jsonObject.getString("value")));
        }
      });
      property.add(map);
    }
    System.out.println("property = " + property);

    Map<String, Integer> status = new HashMap<>();

    //计算
    for(int num = 0; num < split.length; num++){
      Map<String, Boolean> stringBooleanMap = property.get(num);
      System.out.println("stringBooleanMap = " + stringBooleanMap);
      try {
        //1号线
        if (split[num].contains("1BGB")) {
          status.put(split[num], 0);
          if (stringBooleanMap.get("OpenSt") || stringBooleanMap.get("Open1St") || stringBooleanMap.get("Open2St")) {
            status.put(split[num], 4);
          }
          if ((!stringBooleanMap.get("OpenSt") && !stringBooleanMap.get("Open1St") && !stringBooleanMap.get("Open2St"))) {
            status.put(split[num], 3);
          }
          if (stringBooleanMap.get("FailerSt") || stringBooleanMap.get("Failer1St") || stringBooleanMap.get("Failer2St") || stringBooleanMap.get("Failer3St")) {
            status.put(split[num], 2);
          }
          if (stringBooleanMap.get("AlarmSt") || stringBooleanMap.get("LevelSt") || stringBooleanMap.get("Level1St")) {
            status.put(split[num], 1);
          }
        }
        if (split[num].contains("1BAY")) {
          status.put(split[num], 0);
          if (stringBooleanMap.get("OpenSt") || stringBooleanMap.get("Open1St")) {
            status.put(split[num], 4);
          }
          if ((!stringBooleanMap.get("OpenSt") && !stringBooleanMap.get("Open1St"))) {
            status.put(split[num], 3);
          }
          if (stringBooleanMap.get("FailerSt") || stringBooleanMap.get("Failer1St") || stringBooleanMap.get("Failer2St")) {
            status.put(split[num], 2);
          }
          if (stringBooleanMap.get("AlarmSt") || stringBooleanMap.get("LevelSt")) {
            status.put(split[num], 1);
          }
        }

        //2号线
        if (split[num].contains("2BGB")) {
          status.put(split[num], 0);
          if (stringBooleanMap.get("OpenSt") || stringBooleanMap.get("Open1St") || stringBooleanMap.get("Open2St")) {
            status.put(split[num], 4);
          }
          if ((!stringBooleanMap.get("OpenSt") && !stringBooleanMap.get("Open1St") && !stringBooleanMap.get("Open2St"))) {
            status.put(split[num], 3);
          }
          if (stringBooleanMap.get("FailerSt") || stringBooleanMap.get("Failer1St") || stringBooleanMap.get("Failer2St")) {
            status.put(split[num], 2);
          }
          if (stringBooleanMap.get("AlarmSt") || stringBooleanMap.get("LevelSt")) {
            status.put(split[num], 1);
          }
        }
        if (split[num].contains("2BAY")) {
          status.put(split[num], 0);
          if (stringBooleanMap.get("OpenSt") || stringBooleanMap.get("Open1St")) {
            status.put(split[num], 4);
          }
          if ((!stringBooleanMap.get("OpenSt") && !stringBooleanMap.get("Open1St"))) {
            status.put(split[num], 3);
          }
          if (stringBooleanMap.get("FailerSt") || stringBooleanMap.get("Failer1St")) {
            status.put(split[num], 2);
          }
          if (stringBooleanMap.get("AlarmSt") || stringBooleanMap.get("LevelSt")) {
            status.put(split[num], 1);
          }
        }
        if (split[num].contains("2BGI")) {
          status.put(split[num], 0);
          if (stringBooleanMap.get("OpenSt") || stringBooleanMap.get("Open1St")) {
            status.put(split[num], 4);
          }
          if ((!stringBooleanMap.get("OpenSt") && !stringBooleanMap.get("Open1St"))) {
            status.put(split[num], 3);
          }
          if (stringBooleanMap.get("FailerSt") || stringBooleanMap.get("Failer1St")) {
            status.put(split[num], 2);
          }
          if ((stringBooleanMap.get("LevelSt") || !stringBooleanMap.get("LevelSt"))) {
            status.put(split[num], 1);
          }
        }

        //3号线
        if (split[num].contains("3BGA")) {
          status.put(split[num], 0);
          if (!stringBooleanMap.get("H01St") || !stringBooleanMap.get("H02St")) {
            status.put(split[num], 4);
          }
          if (stringBooleanMap.get("H01St") && stringBooleanMap.get("H02St")) {
            status.put(split[num], 3);
          }
          if (!stringBooleanMap.get("H08St") || !stringBooleanMap.get("H09St")) {
            status.put(split[num], 2);
          }
          if (!stringBooleanMap.get("iHHLevSt") || !stringBooleanMap.get("iLLLevSt")) {
            status.put(split[num], 1);
          }
        }
        if (split[num].contains("3BGC")) {
          status.put(split[num], 0);
          if (!stringBooleanMap.get("H01St") || !stringBooleanMap.get("H02St") || !stringBooleanMap.get("H03St")) {
            status.put(split[num], 4);
          }
          if (stringBooleanMap.get("H01St") && stringBooleanMap.get("H02St") && stringBooleanMap.get("H03St")) {
            status.put(split[num], 3);
          }
          if (!stringBooleanMap.get("H08St") || !stringBooleanMap.get("H09St") || !stringBooleanMap.get("H10St")) {
            status.put(split[num], 2);
          }
          if (!stringBooleanMap.get("iHHLevSt") || !stringBooleanMap.get("iLLLevSt")) {
            status.put(split[num], 1);
          }
        }

        //4号线
        if (split[num].contains("BQJB")) {
          status.put(split[num], 0);
          if (stringBooleanMap.get("iRun1St") || stringBooleanMap.get("iRun2St") || stringBooleanMap.get("iRun3St")) {
            status.put(split[num], 4);
          }
          if ((!stringBooleanMap.get("iRun1St") && !stringBooleanMap.get("iRun2St") && !stringBooleanMap.get("iRun3St"))) {
            status.put(split[num], 3);

          }
          if (stringBooleanMap.get("iFault1St") || stringBooleanMap.get("iFault2St") || stringBooleanMap.get("iFault3St")) {
            status.put(split[num], 2);
          }
          if (stringBooleanMap.get("iHHLevSt") || stringBooleanMap.get("iLLLevSt")) {
            status.put(split[num], 1);
          }
        }
        if (split[num].contains("BFSB")) {
          status.put(split[num], 0);
          if (stringBooleanMap.get("iRun1St") || stringBooleanMap.get("iRun2St")) {
            status.put(split[num], 4);
          }
          if ((!stringBooleanMap.get("iRun1St") && !stringBooleanMap.get("iRun2St"))) {
            status.put(split[num], 3);
          }
          if (stringBooleanMap.get("iFault1St") || stringBooleanMap.get("iFault2St")) {
            status.put(split[num], 2);
          }
          if (stringBooleanMap.get("iHHLevSt") || stringBooleanMap.get("iLLLevSt")) {
            status.put(split[num], 1);
          }
        }
      }catch (Exception e){
        e.printStackTrace();
      }
    }

    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, Integer> i : status.entrySet()){
      sb.append(i.getKey()).append("-").append(i.getValue()).append("|");
    }
    sb.toString();
    return ResponseEntity.ok(sb);

  }

  public Boolean changeToBool(String string){
    return Integer.parseInt(string) == 1;
  }


}
