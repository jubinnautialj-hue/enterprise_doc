package com.enterprise.doc.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.doc.common.PageResult;
import com.enterprise.doc.dto.LibraryCreateDTO;
import com.enterprise.doc.entity.Library;
import com.enterprise.doc.entity.LibraryMember;
import com.enterprise.doc.entity.User;
import com.enterprise.doc.exception.BusinessException;
import com.enterprise.doc.mapper.LibraryMapper;
import com.enterprise.doc.mapper.LibraryMemberMapper;
import com.enterprise.doc.mapper.UserMapper;
import com.enterprise.doc.util.SecurityUtils;
import com.enterprise.doc.vo.LibraryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LibraryService {

    private final LibraryMapper libraryMapper;
    private final LibraryMemberMapper libraryMemberMapper;
    private final UserMapper userMapper;

    public LibraryVO create(LibraryCreateDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        Library library = new Library();
        BeanUtils.copyProperties(dto, library);
        library.setOwnerId(userId);
        library.setOwnerName(user.getNickname() != null ? user.getNickname() : user.getUsername());
        library.setStatus(1);
        if (library.getType() == null) {
            library.setType(0);
        }
        libraryMapper.insert(library);

        if (library.getType() == 0) {
            LibraryMember member = new LibraryMember();
            member.setLibraryId(library.getId());
            member.setUserId(userId);
            member.setUserName(library.getOwnerName());
            member.setRole(2);
            libraryMemberMapper.insert(member);
        }

        return toLibraryVO(library);
    }

    public void delete(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Library library = libraryMapper.selectById(id);
        if (library == null) {
            throw new BusinessException("文库不存在");
        }
        if (!library.getOwnerId().equals(userId)) {
            throw new BusinessException("无权限删除该文库");
        }
        libraryMapper.deleteById(id);
    }

    public LibraryVO update(Long id, LibraryCreateDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        Library library = libraryMapper.selectById(id);
        if (library == null) {
            throw new BusinessException("文库不存在");
        }
        if (!library.getOwnerId().equals(userId)) {
            throw new BusinessException("无权限修改该文库");
        }

        library.setName(dto.getName());
        library.setDescription(dto.getDescription());
        if (dto.getCover() != null) {
            library.setCover(dto.getCover());
        }
        libraryMapper.updateById(library);

        return toLibraryVO(library);
    }

    public PageResult<LibraryVO> listPublic(Long current, Long size, String keyword) {
        Long userId = SecurityUtils.getCurrentUserId();

        Page<Library> page = new Page<>(current, size);
        LambdaQueryWrapper<Library> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Library::getType, 0)
                .eq(Library::getStatus, 1);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Library::getName, keyword)
                    .or()
                    .like(Library::getDescription, keyword);
        }
        wrapper.orderByDesc(Library::getCreateTime);

        Page<Library> result = libraryMapper.selectPage(page, wrapper);
        return toPageResult(result);
    }

    public PageResult<LibraryVO> listPersonal(Long current, Long size, String keyword) {
        Long userId = SecurityUtils.getCurrentUserId();

        Page<Library> page = new Page<>(current, size);
        LambdaQueryWrapper<Library> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Library::getType, 1)
                .eq(Library::getOwnerId, userId)
                .eq(Library::getStatus, 1);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Library::getName, keyword)
                    .or()
                    .like(Library::getDescription, keyword);
        }
        wrapper.orderByDesc(Library::getCreateTime);

        Page<Library> result = libraryMapper.selectPage(page, wrapper);
        return toPageResult(result);
    }

    public LibraryVO getById(Long id) {
        Library library = libraryMapper.selectById(id);
        if (library == null) {
            throw new BusinessException("文库不存在");
        }
        return toLibraryVO(library);
    }

    private PageResult<LibraryVO> toPageResult(Page<Library> page) {
        PageResult<LibraryVO> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setRecords(page.getRecords().stream()
                .map(this::toLibraryVO)
                .collect(Collectors.toList()));
        return result;
    }

    private LibraryVO toLibraryVO(Library library) {
        LibraryVO vo = new LibraryVO();
        BeanUtils.copyProperties(library, vo);
        return vo;
    }
}
