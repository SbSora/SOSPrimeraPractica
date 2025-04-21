package com.biblioteca.model;

import java.util.List;

public class ActividadUsuarioDTO {
    private UsuarioDTO user;
    private List<PrestamoDTO> prestact;
    private List<PrestamoDTO> histprest;

    // Getters and Setters
    public UsuarioDTO getUser() {
        return user;
    }

    public void setUser(UsuarioDTO user) {
        this.user = user;
    }

    public List<PrestamoDTO> getprestact() {
        return prestact;
    }

    public void setprestact(List<PrestamoDTO> prestact) {
        this.prestact = prestact;
    }

    public List<PrestamoDTO> gethistprest() {
        return histprest;
    }

    public void sethistprest(List<PrestamoDTO> histprest) {
        this.histprest = histprest;
    }
}