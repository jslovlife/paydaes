package com.paydaes.corehr.commondb.controller;

import com.paydaes.corehr.commondb.service.LeaveTypeService;
import com.paydaes.entities.dto.commondb.LeaveTypeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/corehr/leave-types")
@RequiredArgsConstructor
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    @PostMapping
    public ResponseEntity<LeaveTypeDto> createLeaveType(@RequestBody LeaveTypeDto dto) {
        return new ResponseEntity<>(leaveTypeService.createLeaveType(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveTypeDto> getLeaveTypeById(@PathVariable Long id) {
        return leaveTypeService.getLeaveTypeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<LeaveTypeDto> getLeaveTypeByCode(@PathVariable String code) {
        return leaveTypeService.getLeaveTypeByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<LeaveTypeDto>> getAllLeaveTypes() {
        return ResponseEntity.ok(leaveTypeService.getAllLeaveTypes());
    }

    @GetMapping("/active")
    public ResponseEntity<List<LeaveTypeDto>> getAllActiveLeaveTypes() {
        return ResponseEntity.ok(leaveTypeService.getAllActiveLeaveTypes());
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeaveTypeDto> updateLeaveType(@PathVariable Long id,
                                                        @RequestBody LeaveTypeDto dto) {
        return ResponseEntity.ok(leaveTypeService.updateLeaveType(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<LeaveTypeDto> toggleLeaveType(@PathVariable Long id) {
        return ResponseEntity.ok(leaveTypeService.toggleLeaveType(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeaveType(@PathVariable Long id) {
        leaveTypeService.deleteLeaveType(id);
        return ResponseEntity.noContent().build();
    }
}
