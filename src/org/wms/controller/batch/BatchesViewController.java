package org.wms.controller.batch;

import it.rmautomazioni.controller.listener.AbstractJButtonActionListener;

import java.util.HashMap;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.view.JasperViewer;

import org.wms.config.Utils;
import org.wms.controller.common.AbstractTableSelectionListener;
import org.wms.model.batch.Batch;
import org.wms.model.batch.BatchRow;
import org.wms.model.batch.Batches;
import org.wms.view.batch.BatchView;
import org.wms.view.batch.BatchesView;

/**
 * Controller for batches view
 * 
 * manage all the view actions and connections between BatchesView and BatchesModel
 * 
 * @author Stefano Pessina, Daniele Ciriello
 *
 */
public class BatchesViewController {

	/**
	 * Reference to the batch view
	 */
	protected BatchesView view;
	
	/**
	 * Reference to the batchs model
	 */
	protected Batches batchesModel;
	
	/**
	 * Batch details view JDialog reference
	 */
	protected BatchView batchDialog;
	
	/**
	 * Constructor
	 * add action to view components
	 * 
	 * @param view reference to the batchesView
	 * @param batchesModel reference to the batches model
	 */
	public BatchesViewController(BatchesView view, Batches batchesModel) {
		super();
		this.view = view;
		this.batchesModel = batchesModel;
		
		view.getBtnMarkBatchAsAllocated().setVisible(false);
		view.getBtnPrintBatch().setVisible(false);
		view.getBtnMarkBatchAsComplete().setVisible(false);
		
		new AbstractJButtonActionListener(view.getBtnRefreshBatches()) {
			
			@Override
			public void actionTriggered() {
				btnRefreshAction();
			}
		};
		
		new AbstractJButtonActionListener(view.getBtnMarkBatchAsAllocated()) {
			
			@Override
			public void actionTriggered() {
				btnAllocateAction();
			}
		};
		
		new AbstractJButtonActionListener(view.getBtnPrintBatch()) {
			
			@Override
			public void actionTriggered() {
				btnPrintAction();
			}
		};
		
		new AbstractJButtonActionListener(view.getBtnMarkBatchAsComplete()) {
			
			@Override
			public void actionTriggered() {
				btnCompleteAction();
			}
		};
		
		view.getBatchesTable().addMouseListener(new AbstractTableSelectionListener() {
			
			@Override
			public void validSelectionTrigger(boolean doubleClick, int rowIndex, boolean requireMenu) {
				tblBatchesValidSelectionAction(doubleClick, rowIndex);
			}
			
			@Override
			public void invalidSelectionTriggered() {
				tblBatchesInvalidSelectionAction();
			}
		});
		
	}
	
	
	/**
	 * This method launch the order details view
	 * and its controller
	 * 
	 * @param order order to edit/show
	 * @param isNew true if the model is a new model, false if it's an existent model
	 */
	protected void launchOrderEditView(Batch batch){
		batchDialog = new BatchView(batch);
		new BatchViewController(batchDialog, batch, batchesModel);
		batchDialog.setVisible(true);
	}
	
	/**
	 * This method call the order details view
	 * to create a new order
	 */
	protected void btnRefreshAction() {
		batchesModel.update(batchesModel, null);
	}
	
	/**
	 * This method call the order details view
	 * to edit an existent order
	 */
	protected boolean btnAllocateAction() {
		if(view.getBatchesTable().getSelectedRow()==-1) {
			Utils.msg.errorBox("No order selected", "Error");
			return false;
		}
		
		int rowIndex = view.getBatchesTable().getSelectedRow();
		
		Batch batch = batchesModel.getUnmodificableBatchList().get(rowIndex);
		
		if(!batchesModel.setBatchAsAllocate(batch)) {
			Utils.msg.errorBox("Error during batch allocation", "Error");
			return false;
		}
		
		return true;
	}
	
	/**
	 * This method delete an existent order
	 * selected in the table
	 * if it's possible
	 */
	protected boolean btnPrintAction() {
		if(view.getBatchesTable().getSelectedRow()==-1) {
			Utils.msg.errorBox("No order selected", "Error");
			return false;
		}
		
		int rowIndex = view.getBatchesTable().getSelectedRow();
		
		Batch batch = batchesModel.getUnmodificableBatchList().get(rowIndex);
		
		JasperPrint jasperPrint = null;
        
		String[] columnNames = {"Batch id", "Order id", "Order row", 
				"Material", "Quantity", "Pickup position"};
		List<BatchRow> rows = batch.getRows();
        String[][] data = new String[rows.size()][6];
        
        int i=0;
        for (BatchRow row : rows) {
			data[i][0]=String.valueOf(batch.getId());
			data[i][1]=String.valueOf(row.getReferredOrderRow().getOrder().getId());
			data[i][2]=String.valueOf(row.getReferredOrderRow().getId());
			data[i][3]=String.valueOf(row.getJobWarehouseCell().getMaterial().getCode());
			data[i][4]=String.valueOf(row.getQuantity());
			data[i][5]=row.getJobWarehouseCell().getPublicId();
        	i++;
		}
        
        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
        
        try {
//            JasperCompileManager.compileReportToFile("report/template2.jrxml");
            jasperPrint = JasperFillManager.fillReport("report/template2.jasper", new HashMap(),
                    new JRTableModelDataSource(tableModel));
            JasperViewer jasperViewer = new JasperViewer(jasperPrint);
            jasperViewer.setVisible(true);
        } catch (JRException ex) {
        	Utils.msg.errorBox("Error during start jasper preview tool", "Error");
        	return false;
        }
		
		return true;
	}
	
	/**
	 * This method call mark as complete action
	 */
	protected boolean btnCompleteAction() {
		if(view.getBatchesTable().getSelectedRow()==-1) {
			Utils.msg.errorBox("No order selected", "Error");
			return false;
		}
		
		int rowIndex = view.getBatchesTable().getSelectedRow();
		
		Batch batch = batchesModel.getUnmodificableBatchList().get(rowIndex);
		
		if(!batchesModel.setBatchAsCompleted(batch)) {
			Utils.msg.errorBox("Error during batch mark as completed", "Error");
			return false;
		}
		
		return true;
	}
	
	/**
	 * When the user selected an order in table
	 * the mark as allocated/completed and print buttons will be enabled
	 * 
	 * Moreover if the user press twice
	 * the details view will be automatically showed
	 * 
	 * @param doubleClick if the user press mouse button twice
	 * @param rowIndex index of the table selection
	 */
	protected void tblBatchesValidSelectionAction(boolean doubleClick, int rowIndex) {
		view.getBtnMarkBatchAsAllocated().setVisible(true);
		view.getBtnPrintBatch().setVisible(true);
		view.getBtnMarkBatchAsComplete().setVisible(true);
		
		if(doubleClick) {
			Batch batch = batchesModel.getUnmodificableBatchList().get(rowIndex);
			launchOrderEditView(batch); 
		}
	}
	
	/**
	 * Hide mark as allocated/completed and print buttons
	 */
	protected void tblBatchesInvalidSelectionAction() {
		view.getBtnMarkBatchAsAllocated().setVisible(false);
		view.getBtnPrintBatch().setVisible(false);
		view.getBtnMarkBatchAsComplete().setVisible(false);
	}
}
