package com.example.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.example.entity.Share;
import com.example.mapper.ShareMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 文件分享信息表业务处理
 **/
@Service
public class ShareService {

    @Resource
    private ShareMapper shareMapper;

    /**
     * 新增
     */
    public void add(Share share) {
        shareMapper.insert(share);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        shareMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            shareMapper.deleteById(id);
        }
    }

    /**
     * 修改
     */
    public void updateById(Share share) {
        shareMapper.updateById(share);
    }

    /**
     * 根据ID查询
     */
    public Share selectById(Integer id) {
        Share share = shareMapper.selectById(id);
        this.packageShare(share);
        return share;
    }

    public void updateCount(Integer id) {
        // 访问次数+1
        shareMapper.updateCount(id);
    }

    /**
     * 查询所有
     */
    public List<Share> selectAll(Share share) {
        List<Share> shareList = shareMapper.selectAll(share);
        for (Share s : shareList) {
            this.packageShare(s);
        }
        return shareList;
    }

    private void packageShare(Share share) {
        if (share == null) {
            return;
        }
        String endTime = share.getEndTime();  // 获取截止分享的时间
        DateTime endDatetime = DateUtil.parseDateTime(endTime);
        DateTime now = new DateTime();
        if (endDatetime.isBeforeOrEquals(now)) {  //  比较截止时间跟当前时间的先后顺序
            share.setStatus("已过期");
        } else {  // 截止时间在当前时间之后
            long offsetHours = endDatetime.between(now, DateUnit.HOUR);
            if (offsetHours < 24) {
                share.setStatus(offsetHours + 1 + "小时后过期");
            } else {
                int days = BigDecimal.valueOf(offsetHours).divide(BigDecimal.valueOf(24), 0, RoundingMode.HALF_UP).intValue();
                share.setStatus(days + "天后过期");
            }
        }
    }

    /**
     * 分页查询
     */
    public PageInfo<Share> selectPage(Share share, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Share> list = shareMapper.selectAll(share);
        return PageInfo.of(list);
    }

}