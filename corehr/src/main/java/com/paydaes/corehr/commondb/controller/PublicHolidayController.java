package com.paydaes.corehr.commondb.controller;

import com.paydaes.corehr.commondb.service.PublicHolidayService;
import com.paydaes.entities.dto.commondb.PublicHolidayDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/corehr/public-holidays")
@RequiredArgsConstructor
public class PublicHolidayController {

    private final PublicHolidayService publicHolidayService;

    @PostMapping
    public ResponseEntity<PublicHolidayDto> addHoliday(@RequestBody PublicHolidayDto dto) {
        return new ResponseEntity<>(publicHolidayService.addHoliday(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicHolidayDto> getHolidayById(@PathVariable Long id) {
        return publicHolidayService.getHolidayById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<List<PublicHolidayDto>> getHolidaysByYear(@PathVariable int year) {
        return ResponseEntity.ok(publicHolidayService.getHolidaysByYear(year));
    }

    @GetMapping("/year/{year}/active")
    public ResponseEntity<List<PublicHolidayDto>> getActiveHolidaysByYear(@PathVariable int year) {
        return ResponseEntity.ok(publicHolidayService.getActiveHolidaysByYear(year));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PublicHolidayDto> updateHoliday(@PathVariable Long id,
                                                           @RequestBody PublicHolidayDto dto) {
        return ResponseEntity.ok(publicHolidayService.updateHoliday(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHoliday(@PathVariable Long id) {
        publicHolidayService.deleteHoliday(id);
        return ResponseEntity.noContent().build();
    }
}
