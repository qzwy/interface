package zwy.test.config.controller;

import com.alibaba.fastjson.JSONObject;
import com.sun.org.apache.xpath.internal.operations.Bool;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    //计算
    for(int num = 0; num < split.length; num++){
      //1号线
      JSONObject equipments = JSONObject.parseObject(objects.get(num).toString());
      if (split[num].contains("1BGB")){
        //红色
        Object alarmst = JSONObject.parseObject(equipments.get(split[num] + "_AlarmSt").toString())
            .get("value");
        Object levelst = JSONObject.parseObject(equipments.get(split[num] + "_LevelSt").toString())
            .get("value");
        Object level1st = JSONObject.parseObject(equipments.get(split[num] + "_Level1St").toString())
            .get("value");
        if(changeToBool(alarmst.toString()) || changeToBool(levelst.toString()) || changeToBool(level1st.toString())){
          //todo 红色
          continue;
        }else{

          //黄色
          Object failerst = JSONObject.parseObject(equipments.get(split[num] + "_FailerSt").toString())
              .get("value");
          Object failer1st = JSONObject.parseObject(equipments.get(split[num] + "_Failer1St").toString())
              .get("value");
          Object failer2st = JSONObject.parseObject(equipments.get(split[num] + "_Failer2St").toString())
              .get("value");
          Object failer3st = JSONObject.parseObject(equipments.get(split[num] + "_Failer3St").toString())
              .get("value");

        }

      }
      if (split[num].contains("1BAY")){

      }

      //2号线
      if (split[num].contains("2BGB")){

      }
      if (split[num].contains("2BAY")){

      }
      if (split[num].contains("2BGI")){

      }

      //3号线
      if (split[num].contains("3BGA")){

      }
      if (split[num].contains("3BGC")){

      }

      //4号线
      if (split[num].contains("BQJB")){

      }
      if (split[num].contains("BFSB")){

      }
    }







//    for(int num = 0; num < split.length; num++){
//      relation.put(split[num], objects.get(num));
//    }




    return ResponseEntity.ok(relation);

  }

  public Boolean changeToBool(String string){
    return Integer.parseInt(string) == 1;
  }


}
