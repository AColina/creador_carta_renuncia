/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package colina.angel.model.datamodel;

import com.megagroup.utilidades.StringUtils;

/**
 *
 * @author USRGEMADEV12
 */
public class ProfesionModel {

    private String acronimo;
    private String profesion;

    public ProfesionModel() {
    }

    public ProfesionModel(String acronimo, String profesion) {
        this.acronimo = acronimo;
        this.profesion = profesion;
    }

    public String getAcronimo() {
        return StringUtils.capitalize(acronimo.toLowerCase());
    }

    public void setAcronimo(String acronimo) {
        this.acronimo = acronimo;
    }

    public String getProfesion() {
        return profesion;
    }

    public void setProfesion(String profesion) {
        this.profesion = profesion;
    }

    @Override
    public String toString() {
        return StringUtils.capitalize(profesion.toLowerCase());
    }

}
