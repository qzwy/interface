package zwy.test.config;

import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class DateNum {

// 0 -> 自动日期  非0-> 指定日期
  static String day = "01";

  public static int getDayNum(){
    if (day.equals("0")){
      return LocalDate.now().getDayOfMonth();
    }else {
      return Integer.parseInt(day);
    }
  }
}
