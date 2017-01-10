/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package colina.angel.controller;

import colina.angel.model.combobox.ListModel;
import colina.angel.model.core.CustomValue;
import colina.angel.model.datamodel.ProfesionModel;
import colina.angel.vistas.Creador;
import com.megagroup.binding.model.BindingEvent;
import com.megagroup.model.restricted.AbstractRestrictedTextField;
import com.megagroup.reflection.ReflectionUtils;
import com.megagroup.utilidades.RestrictedTextField;
import com.megagroup.utilidades.StringUtils;
import com.toedter.calendar.JDateChooser;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

/**
 *
 * @author USRGEMADEV12
 */
public class CreadorController extends Creador {

    private static final Logger LOG = Logger.getLogger(CreadorController.class.getName());
    public final SimpleDateFormat format = new SimpleDateFormat("dd 'de' MMMM 'del' yyyy", new Locale("es", "ES"));
    public final String motivoRenuncia = "Dicha decisión responde a motivos estrictamente profesionales, "
            .concat("debido a que quiero mejorar mi carrera profesional y explorar las ")
            .concat("diferentes tecnologías que se encuentran en el mercado, lo cual espero sepa comprender.");
    public static final int CARACTERES_MAXIMO = 1000;
    @CustomValue
    public Date fechaActual;

    public CreadorController() {
        fileChooser.removeChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Docx", "docx"));
        CreadorController.this.changeMotivo(false);
        cargarEventos();
        cargarRestricciones();
        cargarTitulos();
        guardar.setEnabled(false);
        CreadorController.this.validarDateField(fechaingreso);
        CreadorController.this.validarDateField(fechafin);

    }

    public void limpiar() {
        apellido.setText("");
        cargo.setText("");
        cedula.setText("");
        fechafin.setDate(null);
        nombreJefe.setText("");
        apellidoJefe.setText("");
        nombre.setText("");
        motivoAutomatico.setSelected(true);
        motivo.setText("");
        masculino.setSelected(false);
        femenino.setSelected(false);
        fechaingreso.setDate(null);
        this.changeMotivo(false);
    }

    private void procesar() {

        try {
            fechaActual = new Date();
            InputStream st = ClassLoader.getSystemResource("Carta de Renuncia.docx").openStream();

            XWPFDocument document = new XWPFDocument(st);
            Field fields[] = ReflectionUtils.getAllFields(this.getClass());
            document.getParagraphs()
                    .parallelStream()
                    .forEach(p -> {
                        StringBuilder sb = new StringBuilder(p.getText());
                        Arrays.stream(fields)
                                .forEach(f -> {
                                    StringBuilder name = new StringBuilder();
                                    name.append("{").append(f.getName()).append("}");
                                    int start = sb.indexOf(name.toString());
                                    if (start != -1) {
                                        String value = "";
                                        if (f.getAnnotation(CustomValue.class) != null) {
                                            value = getCustomValue(f.getName());
                                        } else if (JDateChooser.class.isAssignableFrom(f.getType())) {
                                            value = formatearFecha(((JDateChooser) ReflectionUtils.getFieldValue(f, this)).getDate());
                                        } else if (JTextField.class.isAssignableFrom(f.getType())) {
                                            value = ((JTextField) ReflectionUtils.getFieldValue(f, this)).getText();
                                        } else if (JTextArea.class.isAssignableFrom(f.getType())) {
                                            value = ((JTextArea) ReflectionUtils.getFieldValue(f, this)).getText();
                                        }
                                        sb.replace(start, start + name.length(), value);
                                        changeText(p, sb.toString());

                                    }
                                });
                    }
                    );

            int opc = fileChooser.showSaveDialog(this);

            if (opc == 0) {
                File f = fileChooser.getSelectedFile();
                if (!f.getName().endsWith(".docx")) {
                    f = new File(f.toString().concat(".docx"));
                }

                FileOutputStream fo = new FileOutputStream(f);
                BufferedOutputStream bos = new BufferedOutputStream(fo);
                document.write(bos);
                JOptionPane.showMessageDialog(this, "La carta ha sido guardada en la siguiente ruta \""
                        .concat(f.toString())
                        .concat("\""),
                        "Carta guardada con exito", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al guardar", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String formatearFecha(Date fecha) {
        return format.format(fecha);
    }

    public void validarDateField(JDateChooser chooser) {
        JComponent spinerInicio = (JComponent) chooser.getJCalendar().getYearChooser().getSpinner();
        for (Component component : spinerInicio.getComponents()) {
            if (component instanceof JTextField) {
                ((JTextField) component).setEnabled(false);
            }
        }
        ((JTextField) chooser.getDateEditor().getUiComponent()).setEditable(false);
    }

    public void changeText(XWPFParagraph paragraph, String newText) {
        List<XWPFRun> runs = paragraph.getRuns();
        for (int i = runs.size() - 1; i > 0; i--) {
            paragraph.removeRun(i);
        }
        XWPFRun run = runs.get(0);
        run.setText(newText, 0);

    }

    public String getCustomValue(String fieldName) {
        switch (fieldName) {
            case "motivo":
                return StringUtils.deleteWhitespace(motivo.getText()).isEmpty()
                        ? motivoRenuncia
                        : motivo.getText();
            case "profesiones":
                return ((ProfesionModel) profesiones.getSelectedItem()).getAcronimo();
            case "fechaActual":
                return formatearFecha(fechaActual);
            case "sexo":
                return masculino.isSelected() ? "Estimado" : "Estimada";
            default:
                throw new RuntimeException("Papi que haceis");
        }
    }

    public void changeMotivo(boolean isVisible) {
        motivo.setVisible(motivoManual.isSelected());
        jScrollPane1.setVisible(motivoManual.isSelected());
        motivo.setText("");
        caracteres.setText(String.valueOf(CARACTERES_MAXIMO));
        caracteres.setVisible(motivoManual.isSelected());
        labalCaracteres.setVisible(motivoManual.isSelected());

        this.revalidate();
    }

    private void cargarTitulos() {
        List<ProfesionModel> lista = new ArrayList<>();
        // <editor-fold defaultstate="collapsed" desc="Procesiones">                          
        lista.add(new ProfesionModel("", ""));
        lista.add(new ProfesionModel("abgdo", "abogado"));
        lista.add(new ProfesionModel("adm", "administrador"));
        lista.add(new ProfesionModel("alcde", "alcalde"));
        lista.add(new ProfesionModel("almte", "almirante"));
        lista.add(new ProfesionModel("anl", "analista"));
        lista.add(new ProfesionModel("arq", "arquitecto"));
        lista.add(new ProfesionModel("bach", "bachiller"));
        lista.add(new ProfesionModel("brig", "brigadier"));
        lista.add(new ProfesionModel("cap", "capitán"));
        lista.add(new ProfesionModel("cnl", "cardenal"));
        lista.add(new ProfesionModel("clg", "clérigo"));
        lista.add(new ProfesionModel("comte", "comandante"));
        lista.add(new ProfesionModel("coord", "coordinador"));
        lista.add(new ProfesionModel("cP", "contador público"));
        lista.add(new ProfesionModel("cnel", "coronel"));
        lista.add(new ProfesionModel("cdor", "contador"));
        lista.add(new ProfesionModel("dir", "director"));
        lista.add(new ProfesionModel("dira", "directora"));
        lista.add(new ProfesionModel("dr", "doctor"));
        lista.add(new ProfesionModel("econ", "economista"));
        lista.add(new ProfesionModel("gte", "gerente"));
        lista.add(new ProfesionModel("gdor", "gobernador"));
        lista.add(new ProfesionModel("gdora", "gobernadora"));
        lista.add(new ProfesionModel("gral", "general"));
        lista.add(new ProfesionModel("ing", "ingeniero"));
        lista.add(new ProfesionModel("jz", "juez"));
        lista.add(new ProfesionModel("lcdo", "licenciado"));
        lista.add(new ProfesionModel("lcda", "licenciada"));
        lista.add(new ProfesionModel("may", "mayor"));
        lista.add(new ProfesionModel("may. Brig", "mayor"));
        lista.add(new ProfesionModel("not", "notario"));
        lista.add(new ProfesionModel("nut", "nutrisionista"));
        lista.add(new ProfesionModel("ob", "obispo"));
        lista.add(new ProfesionModel("odont", "odontologo"));
        lista.add(new ProfesionModel("pdte", "presidente"));
        lista.add(new ProfesionModel("pdta", "presidenta"));
        lista.add(new ProfesionModel("PM", "policia militar"));
        lista.add(new ProfesionModel("prof", "profesor"));
        lista.add(new ProfesionModel("profa", "profesora"));
        lista.add(new ProfesionModel("psic", "psicólogo"));
        lista.add(new ProfesionModel("psiq", "psiquiatra"));
        lista.add(new ProfesionModel("quim", "quimico"));
        lista.add(new ProfesionModel("qF", "quimico farmaceutico"));
        lista.add(new ProfesionModel("soc", "sociólogo"));
        lista.add(new ProfesionModel("superv", "supervisor"));
        lista.add(new ProfesionModel("tle", "teniente"));
        lista.add(new ProfesionModel("tnco", "tecnico"));
        lista.add(new ProfesionModel("TM", "tecnico medico"));
        lista.add(new ProfesionModel("tnlgo", "tecnologo"));
        lista.add(new ProfesionModel("ts", "trabajador social"));
        lista.add(new ProfesionModel("vet", "veterinario"));
        // </editor-fold>                        
        profesiones.setModel(new ListModel<>(lista));
    }

    private void cargarRestricciones() {
        RestrictedTextField rqn = new RestrictedTextField(nombre);
        rqn.setOnlyLetter(true);
        rqn.setLimit(15);
        RestrictedTextField rqa = new RestrictedTextField(apellido);
        rqa.setOnlyLetter(true);
        rqa.setLimit(15);
        RestrictedTextField rqc = new RestrictedTextField(cargo);
        rqc.setOnlyLetter(true);
        rqc.setLimit(45);
        rqc.setAcceptSpace(true);
        RestrictedTextField rqce = new RestrictedTextField(cedula);
        rqce.setOnlyNums(true);
        rqce.setLimit(9);
        RestrictedTextField rqnj = new RestrictedTextField(nombreJefe);
        rqnj.setOnlyLetter(true);
        rqnj.setLimit(15);
        RestrictedTextField rqaj = new RestrictedTextField(apellidoJefe);
        rqaj.setOnlyLetter(true);
        rqaj.setLimit(15);

        motivo.setDocument(new AbstractRestrictedTextField(CARACTERES_MAXIMO));
        fechaingreso.setMaxSelectableDate(new Date());
        fechafin.setMinSelectableDate(new Date());
    }

    private void cargarEventos() {
        guardar.addActionListener(ActionListener -> procesar());
        limpiar.addActionListener(ActionListener -> limpiar());
        cancelar.addActionListener(ActionListener -> System.exit(0));

        motivoManual.getModel().addActionListener(ActionListener -> changeMotivo(true));
        motivoManual.getModel().addActionListener(new ValidEvent());
        motivoAutomatico.getModel().addActionListener(ActionListener -> changeMotivo(false));
        motivoAutomatico.getModel().addActionListener(new ValidEvent());

        nombre.addKeyListener(new ValidEvent());
        apellido.addKeyListener(new ValidEvent());
        cedula.addKeyListener(new ValidEvent());
        cargo.addKeyListener(new ValidEvent());
        nombreJefe.addKeyListener(new ValidEvent());
        apellidoJefe.addKeyListener(new ValidEvent());
        fechaingreso.addPropertyChangeListener(new ValidEvent());
        fechafin.addPropertyChangeListener(new ValidEvent());
        profesiones.addActionListener(new ValidEvent());
        femenino.addActionListener(new ValidEvent());
        masculino.addActionListener(new ValidEvent());
        motivo.addKeyListener(new ValidEvent());

        motivo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                caracteres.setText(String.valueOf(CARACTERES_MAXIMO - motivo.getText().length()));
            }

        });

    }

    public void validarGuardar() {
        boolean valid = true;
        Calendar ingreso = null;
        if (fechaingreso.getDate() != null) {
            ingreso = Calendar.getInstance();
            ingreso.setTime(fechaingreso.getDate());
        }
        Calendar fin = null;
        if (fechafin.getDate() != null) {
            fin = Calendar.getInstance();
            fin.setTime(fechafin.getDate());
        }
        if (StringUtils.isBlank(nombre.getText())) {
            valid = false;
        } else if (StringUtils.isBlank(apellido.getText())) {
            valid = false;
        } else if (StringUtils.isBlank(cedula.getText())) {
            valid = false;
        } else if (StringUtils.isBlank(cargo.getText())) {
            valid = false;
        } else if (StringUtils.isBlank(nombreJefe.getText())) {
            valid = false;
        } else if (StringUtils.isBlank(apellidoJefe.getText())) {
            valid = false;
        } else if (fechaingreso.getDate() == null || fechaingreso.getDate().compareTo(new Date()) == 1) {
            valid = false;
        } else if (fechafin.getDate() == null) {
            valid = false;
        } else if (fechaingreso.getDate().compareTo(fechafin.getDate()) == 1) {
            valid = false;
        } else if (ingreso != null && ingreso.get(Calendar.YEAR) < Calendar.getInstance().get(Calendar.YEAR) - 80) {
            valid = false;
        } else if (fin != null && fin.get(Calendar.YEAR) > Calendar.getInstance().get(Calendar.YEAR) + 1) {
            valid = false;
        } else if (profesiones.getSelectedItem() == null
                || StringUtils.isBlank(profesiones.getSelectedItem().toString())) {
            valid = false;
        } else if (motivoManual.isSelected() && StringUtils.isBlank(motivo.getText())) {
            valid = false;
        } else if (!masculino.isSelected() && !femenino.isSelected()) {
            valid = false;
        }
        guardar.setEnabled(valid);
    }

    private class ValidEvent extends BindingEvent {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equalsIgnoreCase("date")) {
                validarGuardar();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            validarGuardar();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            validarGuardar();
        }

    }

}
