package org.wms.controller.common;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import it.rmautomazioni.view.controls.StatusBarLabel;
import it.rmautomazioni.view.factories.RMColour;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

import org.junit.BeforeClass;
import org.junit.Test;
import org.wms.view.common.MainGUI;

/**
 * Test listeners created in MainGUIController
 * 
 * @author stefano
 *
 */
public class MainGUIControllerUnitTest {
	
	private static MainGUI mockGUI;
	
	private static MainGUIController ctrlTest;
	
	private static StatusBarLabel lblUserTest;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		mockGUI = mock(MainGUI.class);
		
		lblUserTest = new StatusBarLabel(RMColour.RM_DARK_BLUE, RMColour.RM_DARK_BLUE);		
		when(mockGUI.getLblUsers()).thenReturn(lblUserTest);
		
		ctrlTest = new MainGUIController(mockGUI);
	}

	/**
	 * Test popup menu should be attached to the lblUser label
	 */
	@Test
	public void testPopupMenu() {
		assertTrue(lblUserTest.getComponentPopupMenu()!=null);
	}
	
	/**
	 * Test gui should be returned by getGui method
	 */
	@Test
	public void testGetGUI() {
		assertTrue(ctrlTest.getGui().equals(mockGUI));
	}
	
	/**
	 * Test loginPopupMenu should be returned by getPopupMenu method
	 */
	@Test
	public void testGetPopupMenu() {
		assertTrue(ctrlTest.getLoginMenu().equals(ctrlTest.loginMenu));
	}
}