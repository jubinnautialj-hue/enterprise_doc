package com.enterprise.doc.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.enterprise.doc.dto.ShareCreateDTO;
import com.enterprise.doc.dto.ShareVerifyDTO;
import com.enterprise.doc.entity.Document;
import com.enterprise.doc.entity.Share;
import com.enterprise.doc.entity.User;
import com.enterprise.doc.exception.BusinessException;
import com.enterprise.doc.mapper.DocumentMapper;
import com.enterprise.doc.mapper.ShareMapper;
import com.enterprise.doc.mapper.UserMapper;
import com.enterprise.doc.util.SecurityUtils;
import com.enterprise.doc.vo.DocumentVO;
import com.enterprise.doc.vo.ShareVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ShareService {

    private final ShareMapper shareMapper;
    private final DocumentMapper documentMapper;
    private final UserMapper userMapper;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    public ShareVO create(ShareCreateDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (dto.getShareType() == 2) {
            Document doc = documentMapper.selectById(dto.getTargetId());
            if (doc == null) {
                throw new BusinessException("文档不存在");
            }
        }

        String shareCode = IdUtil.simpleUUID().substring(0, 8).toLowerCase();

        Share share = new Share();
        share.setShareType(dto.getShareType());
        share.setTargetId(dto.getTargetId());
        share.setShareCode(shareCode);
        share.setPassword(dto.getPassword());
        share.setExpireType(dto.getExpireType());
        share.setViewCount(0L);
        share.setDownloadCount(0L);
        share.setCreateUserId(userId);
        share.setCreateUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());

        if (dto.getExpireType() != null && dto.getExpireType() > 0) {
            LocalDateTime now = LocalDateTime.now();
            switch (dto.getExpireType()) {
                case 1:
                    share.setExpireTime(now.plusDays(1));
                    break;
                case 2:
                    share.setExpireTime(now.plusDays(7));
                    break;
                case 3:
                    share.setExpireTime(now.plusDays(30));
                    break;
                default:
                    share.setExpireTime(null);
            }
        }

        shareMapper.insert(share);
        share.setShareUrl(contextPath + "/share/" + shareCode);
        shareMapper.updateById(share);

        return toShareVO(share);
    }

    public void cancel(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Share share = shareMapper.selectById(id);
        if (share == null) {
            throw new BusinessException("分享不存在");
        }
        if (!share.getCreateUserId().equals(userId)) {
            throw new BusinessException("无权限取消该分享");
        }
        shareMapper.deleteById(id);
    }

    public ShareVO verify(ShareVerifyDTO dto) {
        Share share = shareMapper.selectOne(new LambdaQueryWrapper<Share>()
                .eq(Share::getShareCode, dto.getShareCode()));
        if (share == null) {
            throw new BusinessException("分享链接无效或已过期");
        }

        if (share.getExpireTime() != null && share.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("分享链接已过期");
        }

        if (share.getPassword() != null && !share.getPassword().isEmpty()) {
            if (dto.getPassword() == null || !dto.getPassword().equals(share.getPassword())) {
                throw new BusinessException("访问密码不正确");
            }
        }

        shareMapper.update(null, new LambdaUpdateWrapper<Share>()
                .eq(Share::getId, share.getId())
                .setSql("view_count = view_count + 1"));

        return toShareVO(share);
    }

    public DocumentVO getShareDocument(String shareCode) {
        Share share = shareMapper.selectOne(new LambdaQueryWrapper<Share>()
                .eq(Share::getShareCode, shareCode));
        if (share == null || share.getShareType() != 2) {
            throw new BusinessException("分享链接无效");
        }

        Document doc = documentMapper.selectById(share.getTargetId());
        if (doc == null) {
            throw new BusinessException("文档不存在");
        }

        documentMapper.update(null, new LambdaUpdateWrapper<Document>()
                .eq(Document::getId, doc.getId())
                .setSql("view_count = view_count + 1"));

        DocumentVO vo = new DocumentVO();
        BeanUtils.copyProperties(doc, vo);
        return vo;
    }

    private ShareVO toShareVO(Share share) {
        ShareVO vo = new ShareVO();
        BeanUtils.copyProperties(share, vo);
        return vo;
    }
}
