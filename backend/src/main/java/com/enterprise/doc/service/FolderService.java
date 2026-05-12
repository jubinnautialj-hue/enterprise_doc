package com.enterprise.doc.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.doc.dto.FolderCreateDTO;
import com.enterprise.doc.entity.Folder;
import com.enterprise.doc.entity.User;
import com.enterprise.doc.exception.BusinessException;
import com.enterprise.doc.mapper.FolderMapper;
import com.enterprise.doc.mapper.UserMapper;
import com.enterprise.doc.util.SecurityUtils;
import com.enterprise.doc.vo.FolderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderMapper folderMapper;
    private final UserMapper userMapper;

    public FolderVO create(FolderCreateDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        Folder parent = null;
        if (dto.getParentId() != null && dto.getParentId() > 0) {
            parent = folderMapper.selectById(dto.getParentId());
            if (parent == null) {
                throw new BusinessException("父文件夹不存在");
            }
        }

        Folder existFolder = folderMapper.selectOne(new LambdaQueryWrapper<Folder>()
                .eq(Folder::getLibraryId, dto.getLibraryId())
                .eq(Folder::getParentId, dto.getParentId() == null ? 0 : dto.getParentId())
                .eq(Folder::getName, dto.getName()));
        if (existFolder != null) {
            throw new BusinessException("文件夹名称已存在");
        }

        Folder folder = new Folder();
        BeanUtils.copyProperties(dto, folder);
        folder.setCreateUserId(userId);
        folder.setCreateUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
        if (parent != null) {
            folder.setPath(parent.getPath() + "/" + folder.getName());
        } else {
            folder.setPath("/" + folder.getName());
        }
        folderMapper.insert(folder);

        return toFolderVO(folder);
    }

    public void delete(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Folder folder = folderMapper.selectById(id);
        if (folder == null) {
            throw new BusinessException("文件夹不存在");
        }
        if (!folder.getCreateUserId().equals(userId)) {
            throw new BusinessException("无权限删除该文件夹");
        }

        List<Folder> children = getAllChildren(id);
        for (Folder child : children) {
            folderMapper.deleteById(child.getId());
        }
        folderMapper.deleteById(id);
    }

    public FolderVO rename(Long id, String name) {
        Long userId = SecurityUtils.getCurrentUserId();
        Folder folder = folderMapper.selectById(id);
        if (folder == null) {
            throw new BusinessException("文件夹不存在");
        }
        if (!folder.getCreateUserId().equals(userId)) {
            throw new BusinessException("无权限重命名该文件夹");
        }

        folder.setName(name);
        folderMapper.updateById(folder);
        return toFolderVO(folder);
    }

    public List<FolderVO> listByLibrary(Long libraryId) {
        List<Folder> folders = folderMapper.selectList(new LambdaQueryWrapper<Folder>()
                .eq(Folder::getLibraryId, libraryId)
                .orderByAsc(Folder::getSort)
                .orderByDesc(Folder::getCreateTime));

        Map<Long, List<FolderVO>> groupByParent = folders.stream()
                .map(this::toFolderVO)
                .collect(Collectors.groupingBy(f -> f.getParentId() == null ? 0L : f.getParentId()));

        List<FolderVO> roots = groupByParent.getOrDefault(0L, new ArrayList<>());
        buildTree(roots, groupByParent);

        return roots;
    }

    public List<FolderVO> listByParent(Long libraryId, Long parentId) {
        List<Folder> folders = folderMapper.selectList(new LambdaQueryWrapper<Folder>()
                .eq(Folder::getLibraryId, libraryId)
                .eq(Folder::getParentId, parentId == null ? 0 : parentId)
                .orderByAsc(Folder::getSort)
                .orderByDesc(Folder::getCreateTime));

        return folders.stream().map(this::toFolderVO).collect(Collectors.toList());
    }

    private List<Folder> getAllChildren(Long parentId) {
        List<Folder> result = new ArrayList<>();
        List<Folder> children = folderMapper.selectList(new LambdaQueryWrapper<Folder>()
                .eq(Folder::getParentId, parentId));
        for (Folder child : children) {
            result.add(child);
            result.addAll(getAllChildren(child.getId()));
        }
        return result;
    }

    private void buildTree(List<FolderVO> nodes, Map<Long, List<FolderVO>> groupByParent) {
        for (FolderVO node : nodes) {
            List<FolderVO> children = groupByParent.get(node.getId());
            if (children != null && !children.isEmpty()) {
                node.setChildren(children);
                buildTree(children, groupByParent);
            }
        }
    }

    private FolderVO toFolderVO(Folder folder) {
        FolderVO vo = new FolderVO();
        BeanUtils.copyProperties(folder, vo);
        return vo;
    }
}
