package com.example.airplanesalehouduan.admin.service;

import com.example.airplanesalehouduan.admin.entity.AdminAircraftType;
import com.example.airplanesalehouduan.admin.repository.AdminAircraftTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 管理员机型管理服务
 */
@Service
public class AdminAircraftTypeService {

    @Autowired
    private AdminAircraftTypeRepository adminAircraftTypeRepository;

    /**
     * 获取所有机型列表
     */
    public List<AdminAircraftType> getAllAircraftTypes() {
        return adminAircraftTypeRepository.findAll();
    }

    /**
     * 根据状态获取机型列表
     */
    public List<AdminAircraftType> getAircraftTypesByStatus(AdminAircraftType.Status status) {
        if (status == null) {
            return adminAircraftTypeRepository.findAll();
        }
        return adminAircraftTypeRepository.findAll().stream()
                .filter(at -> at.getStatus() == status)
                .toList();
    }

    /**
     * 根据ID获取机型
     */
    public Optional<AdminAircraftType> getAircraftTypeById(Integer id) {
        return adminAircraftTypeRepository.findById(id);
    }

    /**
     * 根据类型代码获取机型
     */
    public Optional<AdminAircraftType> getAircraftTypeByTypeCode(String typeCode) {
        return adminAircraftTypeRepository.findByTypeCode(typeCode);
    }

    /**
     * 创建机型
     */
    @Transactional
    public AdminAircraftType createAircraftType(AdminAircraftType aircraftType) {
        // 检查类型代码是否已存在
        if (aircraftType.getTypeCode() != null) {
            Optional<AdminAircraftType> existing = adminAircraftTypeRepository.findByTypeCode(aircraftType.getTypeCode());
            if (existing.isPresent()) {
                throw new RuntimeException("机型代码已存在: " + aircraftType.getTypeCode());
            }
        }

        // 设置默认状态
        if (aircraftType.getStatus() == null) {
            aircraftType.setStatus(AdminAircraftType.Status.active);
        }

        return adminAircraftTypeRepository.save(aircraftType);
    }

    /**
     * 更新机型
     */
    @Transactional
    public AdminAircraftType updateAircraftType(Integer id, AdminAircraftType aircraftType) {
        AdminAircraftType existing = adminAircraftTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("机型不存在，ID: " + id));

        // 如果更新了类型代码，检查是否与其他记录冲突
        if (aircraftType.getTypeCode() != null && !aircraftType.getTypeCode().equals(existing.getTypeCode())) {
            Optional<AdminAircraftType> conflict = adminAircraftTypeRepository.findByTypeCode(aircraftType.getTypeCode());
            if (conflict.isPresent() && !conflict.get().getId().equals(id)) {
                throw new RuntimeException("机型代码已存在: " + aircraftType.getTypeCode());
            }
        }

        // 更新字段
        if (aircraftType.getTypeCode() != null) {
            existing.setTypeCode(aircraftType.getTypeCode());
        }
        if (aircraftType.getManufacturer() != null) {
            existing.setManufacturer(aircraftType.getManufacturer());
        }
        if (aircraftType.getModel() != null) {
            existing.setModel(aircraftType.getModel());
        }
        if (aircraftType.getSeatLayout() != null) {
            existing.setSeatLayout(aircraftType.getSeatLayout());
        }
        if (aircraftType.getStatus() != null) {
            existing.setStatus(aircraftType.getStatus());
        }

        return adminAircraftTypeRepository.save(existing);
    }

    /**
     * 删除机型
     */
    @Transactional
    public void deleteAircraftType(Integer id) {
        AdminAircraftType aircraftType = adminAircraftTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("机型不存在，ID: " + id));
        adminAircraftTypeRepository.delete(aircraftType);
    }

    /**
     * 批量删除机型
     */
    @Transactional
    public void deleteAircraftTypes(List<Integer> ids) {
        for (Integer id : ids) {
            deleteAircraftType(id);
        }
    }

    /**
     * 批量创建机型
     */
    @Transactional
    public List<AdminAircraftType> createAircraftTypes(List<AdminAircraftType> aircraftTypes) {
        for (AdminAircraftType aircraftType : aircraftTypes) {
            // 检查类型代码是否已存在
            if (aircraftType.getTypeCode() != null) {
                Optional<AdminAircraftType> existing = adminAircraftTypeRepository.findByTypeCode(aircraftType.getTypeCode());
                if (existing.isPresent()) {
                    throw new RuntimeException("机型代码已存在: " + aircraftType.getTypeCode());
                }
            }
            // 设置默认状态
            if (aircraftType.getStatus() == null) {
                aircraftType.setStatus(AdminAircraftType.Status.active);
            }
        }
        return adminAircraftTypeRepository.saveAll(aircraftTypes);
    }

    /**
     * 多字段查询机型（分页）
     */
    public Page<AdminAircraftType> searchAircraftTypes(
            String typeCode, String manufacturer, String model, AdminAircraftType.Status status,
            Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        List<AdminAircraftType> allTypes = adminAircraftTypeRepository.findAll();

        List<AdminAircraftType> filtered = allTypes.stream()
                .filter(at -> {
                    // 机型代码筛选
                    if (typeCode != null && !typeCode.trim().isEmpty()) {
                        if (at.getTypeCode() == null || !at.getTypeCode().toLowerCase().contains(typeCode.toLowerCase())) {
                            return false;
                        }
                    }
                    // 制造商筛选
                    if (manufacturer != null && !manufacturer.trim().isEmpty()) {
                        if (at.getManufacturer() == null || !at.getManufacturer().toLowerCase().contains(manufacturer.toLowerCase())) {
                            return false;
                        }
                    }
                    // 型号筛选
                    if (model != null && !model.trim().isEmpty()) {
                        if (at.getModel() == null || !at.getModel().toLowerCase().contains(model.toLowerCase())) {
                            return false;
                        }
                    }
                    // 状态筛选
                    if (status != null && at.getStatus() != status) {
                        return false;
                    }
                    return true;
                })
                .toList();

        // 手动分页
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtered.size());
        List<AdminAircraftType> pageContent = filtered.subList(start, end);

        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, filtered.size());
    }

    /**
     * 分页获取机型列表
     */
    public Page<AdminAircraftType> getAircraftTypesPage(Integer page, Integer size, AdminAircraftType.Status status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        if (status == null) {
            return adminAircraftTypeRepository.findAll(pageable);
        }
        // 由于没有按状态查询的方法，需要先查询所有再过滤
        List<AdminAircraftType> allTypes = adminAircraftTypeRepository.findAll();
        List<AdminAircraftType> filtered = allTypes.stream()
                .filter(at -> at.getStatus() == status)
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtered.size());
        List<AdminAircraftType> pageContent = filtered.subList(start, end);

        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, filtered.size());
    }

    /**
     * 获取所有不重复的制造商列表
     */
    public List<String> getAllManufacturers() {
        return adminAircraftTypeRepository.findAll().stream()
                .map(AdminAircraftType::getManufacturer)
                .filter(m -> m != null && !m.trim().isEmpty())
                .distinct()
                .sorted()
                .toList();
    }

    /**
     * 获取所有不重复的型号列表
     */
    public List<String> getAllModels() {
        return adminAircraftTypeRepository.findAll().stream()
                .map(AdminAircraftType::getModel)
                .filter(m -> m != null && !m.trim().isEmpty())
                .distinct()
                .sorted()
                .toList();
    }
}

