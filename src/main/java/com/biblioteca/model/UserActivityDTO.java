package com.biblioteca.model;

import java.util.List;

public class UserActivityDTO {

    private Usuario usuario;
    private List<PrestamoDTO> activeLoans;
    private List<PrestamoDTO> recentLoans;

    public UserActivityDTO(Usuario usuario, List<PrestamoDTO> activeLoans, List<PrestamoDTO> recentLoans) {
        this.usuario = usuario;
        this.activeLoans = activeLoans;
        this.recentLoans = recentLoans;
    }

    // Getters and setters
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public List<PrestamoDTO> getActiveLoans() { return activeLoans; }
    public void setActiveLoans(List<PrestamoDTO> activeLoans) { this.activeLoans = activeLoans; }
    public List<PrestamoDTO> getRecentLoans() { return recentLoans; }
    public void setRecentLoans(List<PrestamoDTO> recentLoans) { this.recentLoans = recentLoans; }
}