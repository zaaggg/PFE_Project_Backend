package com.PFE.DTT.model;

import jakarta.persistence.*;

@Entity
public class MaintenanceForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    private ControlStandard controlStandard; // Contrôlé selon (None, NFC 15-100, VDE 0100)

    @Enumerated(EnumType.STRING)
    private CurrentType currentType; // Nature du courant (None, AC, DC)

    @Enumerated(EnumType.STRING)
    private NetworkForm networkForm; // Forme du réseau (3, 4 ou 5 conducteurs)

    private String powerCircuit; // Circuit de puissance
    private String controlCircuit; // Circuit de commande
    private String fuseValue; // Valeur de fusible max

    private boolean hasTransformer; // Transformateur (true = Oui, false = Non)

    private String frequency; // Fréquence en Hz

    private String phaseBalanceTest380V; // Test d'équilibrage Phase-Phase 380V
    private String phaseBalanceTest210V; // Test d'équilibrage Phase-Neutre 210V

    private String insulationResistanceMotor; // Résistance d'isolement Moteur > 0.5 Mohm
    private String insulationResistanceCable; // Résistance d'isolement Câble > 0.5 Mohm

    private String machineSizeHeight; // Hauteur de la machine
    private String machineSizeLength; // Longueur de la machine
    private String machineSizeWidth; // Largeur de la machine

    private boolean isInOrder; // ✅ En ordre / ❌ Pas en ordre

    @OneToOne
    @JoinColumn(name = "report_id")
    private Report report;



    // Constructeur par défaut
    public MaintenanceForm() {}

    // Constructeur avec tous les paramètres
    public MaintenanceForm(ControlStandard controlStandard, CurrentType currentType, NetworkForm networkForm,
                           String powerCircuit, String controlCircuit, String fuseValue, boolean hasTransformer,
                           String frequency, String phaseBalanceTest380V, String phaseBalanceTest210V,
                           String insulationResistanceMotor, String insulationResistanceCable,
                           String machineSizeHeight, String machineSizeLength, String machineSizeWidth,
                           boolean isInOrder, User maintenanceTechnician) {
        this.controlStandard = controlStandard;
        this.currentType = currentType;
        this.networkForm = networkForm;
        this.powerCircuit = powerCircuit;
        this.controlCircuit = controlCircuit;
        this.fuseValue = fuseValue;
        this.hasTransformer = hasTransformer;
        this.frequency = frequency;
        this.phaseBalanceTest380V = phaseBalanceTest380V;
        this.phaseBalanceTest210V = phaseBalanceTest210V;
        this.insulationResistanceMotor = insulationResistanceMotor;
        this.insulationResistanceCable = insulationResistanceCable;
        this.machineSizeHeight = machineSizeHeight;
        this.machineSizeLength = machineSizeLength;
        this.machineSizeWidth = machineSizeWidth;
        this.isInOrder = isInOrder;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ControlStandard getControlStandard() {
        return controlStandard;
    }

    public void setControlStandard(ControlStandard controlStandard) {
        this.controlStandard = controlStandard;
    }

    public CurrentType getCurrentType() {
        return currentType;
    }

    public void setCurrentType(CurrentType currentType) {
        this.currentType = currentType;
    }

    public NetworkForm getNetworkForm() {
        return networkForm;
    }

    public void setNetworkForm(NetworkForm networkForm) {
        this.networkForm = networkForm;
    }

    public String getPowerCircuit() {
        return powerCircuit;
    }

    public void setPowerCircuit(String powerCircuit) {
        this.powerCircuit = powerCircuit;
    }

    public String getControlCircuit() {
        return controlCircuit;
    }

    public void setControlCircuit(String controlCircuit) {
        this.controlCircuit = controlCircuit;
    }

    public String getFuseValue() {
        return fuseValue;
    }

    public void setFuseValue(String fuseValue) {
        this.fuseValue = fuseValue;
    }

    public boolean isHasTransformer() {
        return hasTransformer;
    }

    public void setHasTransformer(boolean hasTransformer) {
        this.hasTransformer = hasTransformer;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getPhaseBalanceTest380V() {
        return phaseBalanceTest380V;
    }

    public void setPhaseBalanceTest380V(String phaseBalanceTest380V) {
        this.phaseBalanceTest380V = phaseBalanceTest380V;
    }

    public String getPhaseBalanceTest210V() {
        return phaseBalanceTest210V;
    }

    public void setPhaseBalanceTest210V(String phaseBalanceTest210V) {
        this.phaseBalanceTest210V = phaseBalanceTest210V;
    }

    public String getInsulationResistanceMotor() {
        return insulationResistanceMotor;
    }

    public void setInsulationResistanceMotor(String insulationResistanceMotor) {
        this.insulationResistanceMotor = insulationResistanceMotor;
    }

    public String getInsulationResistanceCable() {
        return insulationResistanceCable;
    }

    public void setInsulationResistanceCable(String insulationResistanceCable) {
        this.insulationResistanceCable = insulationResistanceCable;
    }

    public String getMachineSizeHeight() {
        return machineSizeHeight;
    }

    public void setMachineSizeHeight(String machineSizeHeight) {
        this.machineSizeHeight = machineSizeHeight;
    }

    public String getMachineSizeLength() {
        return machineSizeLength;
    }

    public void setMachineSizeLength(String machineSizeLength) {
        this.machineSizeLength = machineSizeLength;
    }

    public String getMachineSizeWidth() {
        return machineSizeWidth;
    }

    public void setMachineSizeWidth(String machineSizeWidth) {
        this.machineSizeWidth = machineSizeWidth;
    }

    public boolean isInOrder() {
        return isInOrder;
    }

    public void setInOrder(boolean inOrder) {
        isInOrder = inOrder;
    }


}
