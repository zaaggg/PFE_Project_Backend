package com.PFE.DTT.repository;

import com.PFE.DTT.model.MaintenanceForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MaintenanceFormRepository extends JpaRepository<MaintenanceForm, Integer> {
    Optional<MaintenanceForm> findByReportId(int reportId);

    @Query("SELECT CASE WHEN " +
            "mf.powerCircuit IS NOT NULL AND mf.controlCircuit IS NOT NULL AND mf.fuseValue IS NOT NULL AND " +
            "mf.frequency IS NOT NULL AND mf.phaseBalanceTest380v IS NOT NULL AND mf.phaseBalanceTest210v IS NOT NULL AND " +
            "mf.insulationResistanceMotor IS NOT NULL AND mf.insulationResistanceCable IS NOT NULL AND " +
            "mf.machineSizeHeight IS NOT NULL AND mf.machineSizeLength IS NOT NULL AND mf.machineSizeWidth IS NOT NULL " +
            "THEN true ELSE false END " +
            "FROM MaintenanceForm mf WHERE mf.report.id = :reportId")
    boolean isMaintenanceSystemPartFilled(@Param("reportId") Long reportId);

    @Query("SELECT CASE WHEN mf.isInOrder IS NOT NULL THEN true ELSE false END " +
            "FROM MaintenanceForm mf WHERE mf.report.id = :reportId")
    boolean isShePartFilled(@Param("reportId") Long reportId);

}
