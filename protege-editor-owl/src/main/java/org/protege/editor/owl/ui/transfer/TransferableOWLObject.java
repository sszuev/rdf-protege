package org.protege.editor.owl.ui.transfer;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLObject;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 04-Jun-2006<br><br>

 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class TransferableOWLObject implements Transferable {

    private Map<DataFlavor, TransferHandler> dataFlavorMap;

    public TransferableOWLObject(final OWLModelManager owlModelManager, List<?> objects) {
        dataFlavorMap = new HashMap<>();
        dataFlavorMap.put(OWLObjectDataFlavor.OWL_OBJECT_DATA_FLAVOR, () -> new ArrayList<>(objects));

        TransferHandler stringTransferHandler = () -> {
            StringBuilder builder = new StringBuilder();
            for (Object obj : objects) {
                builder.append(toString(owlModelManager, obj));
                builder.append("\n");
            }
            return builder.toString();
        };
        dataFlavorMap.put(DataFlavor.stringFlavor, stringTransferHandler);
    }

    protected String toString(OWLModelManager manager, Object obj) {
        return obj instanceof OWLObject ? manager.getRendering((OWLObject) obj) : String.valueOf(obj);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        DataFlavor[] dataFlavors = new DataFlavor[dataFlavorMap.size()]; // todo: wtf?
        System.arraycopy(dataFlavorMap.keySet().toArray(), 0, dataFlavors, 0, dataFlavors.length);
        return dataFlavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return dataFlavorMap.containsKey(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        TransferHandler handler = dataFlavorMap.get(flavor);
        if (handler == null) {
            throw new UnsupportedFlavorException(flavor);
        }
        return handler.getTransferData();
    }

    private interface TransferHandler {

        Object getTransferData();
    }
}
