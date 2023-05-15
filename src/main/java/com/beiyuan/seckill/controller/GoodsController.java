package com.beiyuan.seckill.controller;

import com.beiyuan.seckill.entity.User;
import com.beiyuan.seckill.service.IGoodsService;
import com.beiyuan.seckill.service.IUserService;
import com.beiyuan.seckill.vo.DetailVo;
import com.beiyuan.seckill.vo.GoodsVo;
import com.beiyuan.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author: beiyuan
 * @date: 2023/5/1  14:47
 */
@RequestMapping("goods")
@Controller
public class GoodsController {

    @Autowired
    IUserService userService;
    @Autowired
    IGoodsService goodsService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;
    /**
     * 跳转到商品页面
     * 并对页面进行缓存
     * @param model
     *
     * @return
     */
    @GetMapping(value = "toList",produces = "text/html;charset=utf-8") //结果类型
    @ResponseBody
    //@GetMapping(value = "toList")
    public String toList( Model model, User user,HttpServletRequest request,HttpServletResponse response) {
//        //没有cookie,去登陆页面。每次有User参数都要验证，太麻烦，用参数解析器解决
//        if(StringUtils.isEmpty(ticket)){
//            return "login";
//        }
//        //有cookie但是不对
//        //User user=(User) session.getAttribute(ticket);  //不用session存信息了
//        User user=userService.getUserByRedisCookie(ticket,request,response);


//        if(user==null){     //商品列表可以不登陆查看
//            return "login";
//        }
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //html是渲染好的页面
        String html=(String)valueOperations.get("goodsList");
        if(!StringUtils.isEmpty(html)){
            return html;
        }

        //设置一些值,这里会把用户所有信息包括密码传递过去，不过密码是二次加密的了
        model.addAttribute("user",user);
        model.addAttribute("goodsList",goodsService.findGoodsVo());//不缓存就每次都执行到这一行查数据库
       // return "goodsList";
        //手动渲染
        WebContext webContext=new WebContext(request,response,
                request.getServletContext(),request.getLocale(),model.asMap());//model.asMap()，要放入到页面的数据
        html=thymeleafViewResolver.getTemplateEngine().process("goodsList",webContext);
        //存入redis中进行缓存
        if(!StringUtils.isEmpty(html)){
            valueOperations.set("goodsList",html,30, TimeUnit.SECONDS);
        }
        return html;
    }

    /*
    原理的页面，只做了整个页面的静态化
    对不同goodsId的页面进行缓存（url缓存）
     */
    @GetMapping(value = "detail2/{goodsId}",produces = "text/html;charset=utf-8")
    @ResponseBody
    public String tooDetail2(Model model,User user,@PathVariable  Long goodsId,HttpServletRequest request,HttpServletResponse response){
        //从redis中获取页面
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html= (String) valueOperations.get("goodsDetail"+goodsId);
        if(!StringUtils.isEmpty(html)){
            return html;
        }

        model.addAttribute("user",user);
        //这个方法就很多余，上一步已经拿到了所有的goods信息，这里只是根据id再拿到对应的那个。
        GoodsVo goodsVo=goodsService.findGoodsVoByGoodsId(goodsId);


        //给前端整理一些信息
        Date startDate=goodsVo.getStartDate();
        Date endDate=goodsVo.getEndDate();
        Date nowDate=new Date();
        //秒杀状态
        int seckillStatus=0;
        //倒计时
        int remainSeconds=0;
        //还没开始
        if(nowDate.before(startDate)){
            remainSeconds=(int)(startDate.getTime()-nowDate.getTime())/1000;
        }else if(nowDate.after(endDate)){
            //秒杀已经结束
            seckillStatus=2;
        }else {
            //秒杀中
            seckillStatus=1;
        }

        model.addAttribute("secKillStatus",seckillStatus);
        model.addAttribute("remainSeconds",remainSeconds);
        model.addAttribute("goods",goodsVo);

        WebContext webContext=new WebContext(request,response,request.getServletContext(),request.getLocale(),model.asMap());
        //相当于给goodsDetail.html模板进行渲染
        html=thymeleafViewResolver.getTemplateEngine().process("goodsDetail",webContext);  //模板名要已有的
        if(!StringUtils.isEmpty(html)){
            valueOperations.set("goodsDetail"+goodsId,html,30,TimeUnit.SECONDS);
        }
        return html;
    }

    /*
    新的页面，对页面进行了静态化，只传输一些数据
 对不同goodsId的页面进行缓存（url缓存）
  */
    @GetMapping(value = "detail/{goodsId}")
    @ResponseBody
    public RespBean tooDetail(Model model, User user, @PathVariable  Long goodsId){



        //这个方法就很多余，上一步已经拿到了所有的goods信息，这里只是根据id再拿到对应的那个。
        GoodsVo goodsVo=goodsService.findGoodsVoByGoodsId(goodsId);


        //给前端整理一些信息
        Date startDate=goodsVo.getStartDate();
        Date endDate=goodsVo.getEndDate();
        Date nowDate=new Date();
        //秒杀状态
        int seckillStatus=0;
        //倒计时
        int remainSeconds=0;
        //还没开始
        if(nowDate.before(startDate)){
            remainSeconds=(int)(startDate.getTime()-nowDate.getTime())/1000;
        }else if(nowDate.after(endDate)){
            //秒杀已经结束
            seckillStatus=2;
        }else {
            //秒杀中
            seckillStatus=1;
        }
        DetailVo detailVo = new DetailVo(user, goodsVo, seckillStatus, remainSeconds);

        return RespBean.success(detailVo);
    }
}
