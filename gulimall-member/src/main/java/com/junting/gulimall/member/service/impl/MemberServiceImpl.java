package com.junting.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.junting.common.utils.HttpUtils;
import com.junting.common.vo.giteeUidVo;
import com.junting.gulimall.member.dao.MemberLevelDao;
import com.junting.gulimall.member.entity.MemberLevelEntity;
import com.junting.gulimall.member.exception.PhoneExistException;
import com.junting.gulimall.member.exception.UserNameExistException;
import com.junting.gulimall.member.vo.MemberLoginVo;
import com.junting.gulimall.member.vo.SocialUser;
import com.junting.gulimall.member.vo.UserRegisterVo;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.asn1.x500.style.RFC4519Style;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.junting.common.utils.PageUtils;
import com.junting.common.utils.Query;

import com.junting.gulimall.member.dao.MemberDao;
import com.junting.gulimall.member.entity.MemberEntity;
import com.junting.gulimall.member.service.MemberService;

import static org.bouncycastle.asn1.x500.style.RFC4519Style.uid;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(UserRegisterVo userRegisterVo) throws PhoneExistException, UserNameExistException {
        //保存用户信息到数据库
        MemberEntity entity = new MemberEntity();
        // 设置默认等级
        MemberLevelEntity memberLevelEntity = memberLevelDao.getDefaultLevel();
        entity.setLevelId(memberLevelEntity.getId());

        //判断用户名和手机or邮箱是否唯一，需要实现登录
        // 检查手机号 用户名是否唯一 // 不一致则抛出异常
        checkPhone(userRegisterVo.getPhone());
        checkUserName(userRegisterVo.getUserName());
        entity.setUsername(userRegisterVo.getUserName());
        entity.setMobile(userRegisterVo.getPhone());

        //设置密码，需要加密处理，使用MD5加盐加密，MD5不可逆，需要在数据库添加盐字段
        //使用以下spring API可以不添加字段，拿用户登录时输入的password与encode可得true or false
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encode = encoder.encode(userRegisterVo.getPassword());
        entity.setPassword(encode);

        // 其他的默认信息
        entity.setCity("湖南 长沙");
        entity.setCreateTime(new Date());
        entity.setStatus(0);
        entity.setNickname(userRegisterVo.getUserName());
        entity.setBirth(new Date());
        entity.setEmail("xxx@gmail.com");
        entity.setGender(1);
        entity.setJob("JAVA");
        baseMapper.insert(entity);

    }

    //在接口中声明异常，调用时可以确定是否需要处理
    @Override   // void 无需bool // 自定义异常继承 extends RuntimeException
    public void checkPhone(String phone) throws PhoneExistException {
        if (this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone)) > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUserName(String username) throws UserNameExistException {
        if (this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username)) > 0) {
            throw new UserNameExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginacct = vo.getLoginacct();
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        // 去数据库查询
        MemberEntity entity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", loginacct));
        if (entity == null) {
            // 登录失败
            return null;
        } else {
            // 前面传一个明文密码 后面传一个编码后的密码
            boolean matches = bCryptPasswordEncoder.matches(vo.getPassword(), entity.getPassword());
            if (matches) {
//                entity.setPassword(null);
                return entity;
            } else {
                return null;
            }
        }
    }

    @Override   // 已经用code生成了token
    public MemberEntity login(SocialUser socialUser) {
        HashMap<String, String> map = new HashMap<>();
        giteeUidVo giteeUidVo = new giteeUidVo();
        HttpResponse response = null;
        String json = null;
        try {
            map.put("access_token", socialUser.getAccessToken());
            response = HttpUtils.doGet("https://gitee.com", "/api/v5/user", "get", new HashMap<>(), map);
            String jsonGitee = EntityUtils.toString(response.getEntity());
            giteeUidVo = JSON.parseObject(jsonGitee, giteeUidVo.class);
            // 查询成功
//            json = EntityUtils.toString(response.getEntity());
        }catch (Exception e){
            log.warn("社交登录时远程调用出错 [尝试修复]");
        }
        // 微博的uid   gitee
//        String uid = socialUser.getUid();
        socialUser.setUid(giteeUidVo.getName());
        // 1.判断社交用户登录过系统
        MemberEntity entity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", socialUser.getUid()));

        MemberEntity memberEntity = new MemberEntity();
        if (entity != null) { // 注册过
            // 说明这个用户注册过, 修改它的资料
            // 更新令牌
            memberEntity.setId(entity.getId());
            memberEntity.setAccessToken(socialUser.getAccessToken());
            memberEntity.setExpiresIn(socialUser.getExpiresIn());
            // 更新
            baseMapper.updateById(memberEntity);

            entity.setAccessToken(socialUser.getAccessToken());
            entity.setExpiresIn(socialUser.getExpiresIn());
            entity.setPassword(null);
            return entity;
        } else { // 没有注册过
            // 2. 没有查到当前社交用户对应的记录 我们就需要注册一个
//            HashMap<String, String> map = new HashMap<>();
//            map.put("access_token", socialUser.getAccessToken());
//            map.put("uid", socialUser.getUid());
            // 3. 查询当前社交用户账号信息(昵称、性别、头像等)
//                HttpResponse response = HttpUtils.doGet("https://gitee.com", "/api/v5/user", "get", new HashMap<>(), map);
            if (response.getStatusLine().getStatusCode() == 200) {
//                // 查询成功
//                String json = EntityUtils.toString(response.getEntity());
                // 这个JSON对象什么样的数据都可以直接获取
//                JSONObject jsonObject = JSON.parseObject(json);
                memberEntity.setNickname(giteeUidVo.getName());
                memberEntity.setUsername(giteeUidVo.getName());
//                memberEntity.setGender("m".equals(jsonObject.getString("gender")) ? 1 : 0);
//                memberEntity.setCity(jsonObject.getString("location"));
                memberEntity.setJob("自媒体");
                memberEntity.setEmail(giteeUidVo.getEmail());
            }
            memberEntity.setStatus(0);
            memberEntity.setCreateTime(new Date());
            memberEntity.setBirth(new Date());
            memberEntity.setLevelId(1L);
            memberEntity.setSocialUid(socialUser.getUid());
            memberEntity.setAccessToken(socialUser.getAccessToken());
            memberEntity.setExpiresIn(socialUser.getExpiresIn());

            // 注册 -- 登录成功
            baseMapper.insert(memberEntity);
            memberEntity.setPassword(null);
            return memberEntity;
        }
    }

}