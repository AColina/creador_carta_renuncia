/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package colina.angel.model.combobox;

import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 *
 * @author USRGEMADEV12
 */
public class ListModel<E> extends AbstractListModel<E> implements ComboBoxModel<E> {

    private List<E> data;
    private int index;

    public ListModel() {
        data = new ArrayList<>();
    }

    public ListModel(List<E> data) {
        this.data = data;
    }

    public void add(E element) {
        data.add(element);
        fireIntervalAdded(this, data.size() - 1, data.size());
    }

    @Override
    public int getSize() {
        return data.size();
    }

    @Override
    public E getElementAt(int index) {
        return data.get(index);
    }

    @Override
    public void setSelectedItem(Object anItem) {
        index = data.indexOf(anItem);
    }

    @Override
    public Object getSelectedItem() {
        return index == -1 ? null : data.get(index);
    }

    public void setData(List<E> data) {
        this.data = data;
        fireContentsChanged(this, 0, data.size());
    }

    public Object[] getData() {
        return data.toArray();
    }

}
