/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.chart.ui.swt.wizard.format.popup.chart;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.ChartWithoutAxes;
import org.eclipse.birt.chart.model.attribute.ChartDimension;
import org.eclipse.birt.chart.model.attribute.Orientation;
import org.eclipse.birt.chart.model.attribute.UnitsOfMeasurement;
import org.eclipse.birt.chart.model.attribute.impl.TextImpl;
import org.eclipse.birt.chart.model.util.ChartDefaultValueUtil;
import org.eclipse.birt.chart.ui.extension.i18n.Messages;
import org.eclipse.birt.chart.ui.swt.composites.ExternalizedTextEditorComposite;
import org.eclipse.birt.chart.ui.swt.composites.LocalizedNumberEditorComposite;
import org.eclipse.birt.chart.ui.swt.fieldassist.TextNumberEditorAssistField;
import org.eclipse.birt.chart.ui.swt.interfaces.IChartSubType;
import org.eclipse.birt.chart.ui.swt.wizard.ChartWizardContext;
import org.eclipse.birt.chart.ui.swt.wizard.format.popup.AbstractPopupSheet;
import org.eclipse.birt.chart.ui.util.ChartHelpContextIds;
import org.eclipse.birt.chart.ui.util.ChartUIExtensionUtil;
import org.eclipse.birt.chart.ui.util.ChartUIUtil;
import org.eclipse.birt.chart.util.LiteralHelper;
import org.eclipse.birt.chart.util.NameSet;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

/**
 * GeneralPropertiesChartSheet
 */

public class GeneralPropertiesChartSheet extends AbstractPopupSheet implements
		Listener,
		ModifyListener,
		SelectionListener
{

	private Composite cmpContent;

	private ExternalizedTextEditorComposite txtDescription;

	private Text txtType;

	private Text txtSubType;

	private Spinner txtUnitSpacing;

	private Combo cmbUnits;

	private Label lblSeriesThickness;

	private LocalizedNumberEditorComposite txtSeriesThickness;

	private Spinner iscColumnCount;

	private String sOldUnits = ""; //$NON-NLS-1$

	private Button btnSeriesThickness;

	private Button btnColumnCount;

	private Button btnUnitSpacing;

	public GeneralPropertiesChartSheet( String title, ChartWizardContext context )
	{
		super( title, context, false );
	}
	
	@Override
	protected void bindHelp( Composite parent )
	{
		ChartUIUtil.bindHelp( parent, ChartHelpContextIds.POPUP_CHART_GENERAL );
	}

	protected Composite getComponent( Composite parent )
	{
		cmpContent = new Composite( parent, SWT.NONE );
		{
			GridLayout glContent = new GridLayout( );
			glContent.horizontalSpacing = 5;
			glContent.verticalSpacing = 5;
			glContent.marginHeight = 7;
			glContent.marginWidth = 7;
			cmpContent.setLayout( glContent );
		}

		// Layout for General composite
		GridLayout glGeneral = new GridLayout( );
		glGeneral.numColumns = 3;
		glGeneral.horizontalSpacing = 5;
		glGeneral.verticalSpacing = 5;
		glGeneral.marginHeight = 7;
		glGeneral.marginWidth = 7;

		createDescriptionArea( cmpContent );

		Group grpGeneral = new Group( cmpContent, SWT.NONE );
		GridData gdGRPGeneral = new GridData( GridData.VERTICAL_ALIGN_BEGINNING
				| GridData.FILL_HORIZONTAL );
		grpGeneral.setLayoutData( gdGRPGeneral );
		grpGeneral.setLayout( glGeneral );
		grpGeneral.setText( Messages.getString( "AttributeSheetImpl.Lbl.ChartProperties" ) ); //$NON-NLS-1$

		Label lblType = new Label( grpGeneral, SWT.NONE );
		GridData gdLBLType = new GridData( );
		gdLBLType.horizontalIndent = 1;
		lblType.setLayoutData( gdLBLType );
		lblType.setText( Messages.getString( "AttributeSheetImpl.Lbl.Type" ) ); //$NON-NLS-1$

		txtType = new Text( grpGeneral, SWT.BORDER | SWT.READ_ONLY );
		GridData gdTXTType = new GridData( GridData.FILL_HORIZONTAL );
		gdTXTType.horizontalSpan = 2;
		txtType.setLayoutData( gdTXTType );
		txtType.setText( getContext( ).getChartType( ).getDisplayName( ) );

		Label lblSubType = new Label( grpGeneral, SWT.NONE );
		GridData gdLBLSubType = new GridData( );
		gdLBLSubType.horizontalIndent = 1;
		lblSubType.setLayoutData( gdLBLSubType );
		lblSubType.setText( Messages.getString( "AttributeSheetImpl.Lbl.Subtype" ) ); //$NON-NLS-1$

		txtSubType = new Text( grpGeneral, SWT.BORDER | SWT.READ_ONLY );
		GridData gdTXTSubType = new GridData( GridData.FILL_HORIZONTAL );
		gdTXTSubType.horizontalSpan = 2;
		txtSubType.setLayoutData( gdTXTSubType );
		txtSubType.setText( "" );//$NON-NLS-1$

		Orientation orientation = Orientation.VERTICAL_LITERAL;
		if ( getChart( ) instanceof ChartWithAxes )
		{
			orientation = ( (ChartWithAxes) getChart( ) ).getOrientation( );
		}
		Vector<IChartSubType> vSubType = (Vector<IChartSubType>) getContext( ).getChartType( )
				.getChartSubtypes( getChart( ).getDimension( ).getName( ),
						orientation );
		Iterator<IChartSubType> iter = vSubType.iterator( );
		while ( iter.hasNext( ) )
		{
			IChartSubType cSubType = iter.next( );
			if ( cSubType.getName( ).equals( getChart( ).getSubType( ) ) )
			{
				txtSubType.setText( cSubType.getDisplayName( ) );
				break;
			}
		}

		createMisc( grpGeneral );

		return cmpContent;
	}

	protected void createMisc( Group grpGeneral )
	{
		if ( getChart( ).getDimension( ).getValue( ) == ChartDimension.TWO_DIMENSIONAL_WITH_DEPTH )
		{
			Label lblUnit = new Label( grpGeneral, SWT.NONE );
			GridData gdLBLUnit = new GridData( );
			gdLBLUnit.horizontalIndent = 1;
			lblUnit.setLayoutData( gdLBLUnit );
			lblUnit.setText( Messages.getString( "AttributeSheetImpl.Lbl.Units" ) ); //$NON-NLS-1$

			cmbUnits = new Combo( grpGeneral, SWT.DROP_DOWN | SWT.READ_ONLY );
			GridData gdCMBUnits = new GridData( GridData.FILL_HORIZONTAL );
			gdCMBUnits.horizontalSpan = 2;
			cmbUnits.setLayoutData( gdCMBUnits );
			cmbUnits.addSelectionListener( this );

			lblSeriesThickness = new Label( grpGeneral, SWT.NONE );
			GridData gdLBLSeriesThickness = new GridData( );
			gdLBLSeriesThickness.horizontalIndent = 1;
			lblSeriesThickness.setLayoutData( gdLBLSeriesThickness );

			txtSeriesThickness = new LocalizedNumberEditorComposite( grpGeneral,
					SWT.BORDER | SWT.SINGLE );
			new TextNumberEditorAssistField( txtSeriesThickness.getTextControl( ),
					null );

			GridData gdTXTSeriesThickness = new GridData( GridData.FILL_HORIZONTAL );
			txtSeriesThickness.setLayoutData( gdTXTSeriesThickness );
			double dblPoints = getChart( ).getSeriesThickness( );
			double dblCurrent = getContext( ).getUIServiceProvider( )
					.getConvertedValue( dblPoints,
							"Points", getUnits( ) ); //$NON-NLS-1$
			txtSeriesThickness.setValue( dblCurrent );
			txtSeriesThickness.addModifyListener( this );

			btnSeriesThickness = new Button( grpGeneral, SWT.CHECK );
			btnSeriesThickness.setText( ChartUIExtensionUtil.getAutoMessage( ) );
			btnSeriesThickness.setSelection( !getChart( ).isSetSeriesThickness( ) );
			btnSeriesThickness.addSelectionListener( this );
			txtSeriesThickness.setEnabled( !btnSeriesThickness.getSelection( ) );
			populateLists( );
		}

		if ( getChart( ) instanceof ChartWithoutAxes )
		{
			Label lblColumnCount = new Label( grpGeneral, SWT.NONE );
			GridData gdLBLColumnCount = new GridData( );
			gdLBLColumnCount.horizontalIndent = 1;
			lblColumnCount.setLayoutData( gdLBLColumnCount );
			lblColumnCount.setText( Messages.getString( "AttributeSheetImpl.Lbl.ColumnCount" ) ); //$NON-NLS-1$

			iscColumnCount = new Spinner( grpGeneral, SWT.BORDER );
			GridData gdISCColumnCount = new GridData( GridData.FILL_HORIZONTAL );
			iscColumnCount.setLayoutData( gdISCColumnCount );
			iscColumnCount.setMinimum( 0 );
			iscColumnCount.setMaximum( 5 );
			iscColumnCount.setSelection( getChart( ).getGridColumnCount( ) );
			iscColumnCount.addSelectionListener( this );

			btnColumnCount = new Button( grpGeneral, SWT.CHECK );
			btnColumnCount.setText( ChartUIExtensionUtil.getAutoMessage( ) );
			btnColumnCount.setSelection( !getChart( ).isSetGridColumnCount( ) );
			btnColumnCount.addSelectionListener( this );
			iscColumnCount.setEnabled( !btnColumnCount.getSelection( ) );
		}

		else if ( getChart( ) instanceof ChartWithAxes )
		{
			Label lblUnitSpacing = new Label( grpGeneral, SWT.NONE );
			GridData gdUnitSpacing = new GridData( );
			gdUnitSpacing.horizontalIndent = 1;
			lblUnitSpacing.setLayoutData( gdUnitSpacing );
			lblUnitSpacing.setText( Messages.getString( "AttributeSheetImpl.Lbl.UnitSpacing" ) ); //$NON-NLS-1$

			txtUnitSpacing = new Spinner( grpGeneral, SWT.BORDER );
			GridData gdTXTUnitSpacing = new GridData( GridData.FILL_HORIZONTAL );
			txtUnitSpacing.setLayoutData( gdTXTUnitSpacing );
			txtUnitSpacing.setMinimum( 0 );
			txtUnitSpacing.setMaximum( 100 );
			txtUnitSpacing.setIncrement( 1 );
			double unitSpacing = ( (ChartWithAxes) getChart( ) ).getUnitSpacing( );
			txtUnitSpacing.setSelection( (int) unitSpacing );
			txtUnitSpacing.addSelectionListener( this );

			btnUnitSpacing = new Button( grpGeneral, SWT.CHECK );
			btnUnitSpacing.setText( ChartUIExtensionUtil.getAutoMessage( ) );
			btnUnitSpacing.setSelection( !( (ChartWithAxes) getChart( ) ).isSetUnitSpacing( ) );
			btnUnitSpacing.addSelectionListener( this );
			txtUnitSpacing.setEnabled( !btnUnitSpacing.getSelection( ) );
		}
	}

	private void createDescriptionArea( Composite parent )
	{
		Composite cmpDesp = new Composite( parent, SWT.NONE );
		{
			cmpDesp.setLayout( new GridLayout( 2, false ) );
			GridData griddata = new GridData( );
			griddata.horizontalAlignment = SWT.FILL;
			griddata.widthHint = 300;
			cmpDesp.setLayoutData( griddata );
		}

		List<String> keys = null;
		if ( getContext( ).getUIServiceProvider( ) != null )
		{
			keys = getContext( ).getUIServiceProvider( ).getRegisteredKeys( );
		}

		Label lblDescription = new Label( cmpDesp, SWT.NONE );
		GridData gdLBLDescription = new GridData( GridData.VERTICAL_ALIGN_BEGINNING );
		gdLBLDescription.horizontalIndent = 2;
		gdLBLDescription.grabExcessHorizontalSpace = false;
		lblDescription.setLayoutData( gdLBLDescription );
		lblDescription.setText( Messages.getString( "GeneralSheetImpl.Lbl.Description" ) ); //$NON-NLS-1$

		String sDescription = ""; //$NON-NLS-1$
		if ( getChart( ).getDescription( ) != null )
		{
			sDescription = getChart( ).getDescription( ).getValue( );
		}
		txtDescription = new ExternalizedTextEditorComposite( cmpDesp,
				SWT.BORDER | SWT.MULTI | SWT.WRAP,
				65,
				-1,
				keys,
				getContext( ).getUIServiceProvider( ),
				sDescription );
		GridData gdTXTDescription = new GridData( GridData.FILL_HORIZONTAL );
		gdTXTDescription.heightHint = 65;
		txtDescription.setLayoutData( gdTXTDescription );
		txtDescription.addListener( this );
	}

	private void populateLists( )
	{
		NameSet ns = LiteralHelper.unitsOfMeasurementSet;
		cmbUnits.setItems( ChartUIExtensionUtil.getItemsWithAuto( ns.getDisplayNames( ) ) );

		String str = getUnits( );
		if ( str != null && str.trim( ).length( ) != 0 )
		{
			cmbUnits.setText( ns.getDisplayNameByName( str ) );
		}
		else
		{
			cmbUnits.select( 0 ); // Auto case.
		}
		this.sOldUnits = ns.getNameByDisplayName( cmbUnits.getText( ) );
		lblSeriesThickness.setText( new MessageFormat( Messages.getString( "GeneralSheetImpl.Lbl.SeriesWidth" ) ).format( new Object[]{LiteralHelper.unitsOfMeasurementSet.getDisplayNameByName( getUnits( ) )} ) ); //$NON-NLS-1$
	}

	private String getUnits( )
	{
		String units = getChart( ).getUnits( );
		if ( units == null )
		{
			units = ChartDefaultValueUtil.getDefaultUnits( getChart( ) );
		}
		return units;
	}

	private double recalculateUnitDependentValues( double value )
	{
		return getContext( ).getUIServiceProvider( )
				.getConvertedValue( value,
						sOldUnits,
						LiteralHelper.unitsOfMeasurementSet.getNameByDisplayName( cmbUnits.getText( ) ) );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events
	 * .ModifyEvent)
	 */
	public void modifyText( ModifyEvent e )
	{
		if ( e.widget.equals( txtSeriesThickness ) )
		{
			updateToSeriesThickness( );
		}

	}

	protected void updateToSeriesThickness( )
	{
		double dblCurrent = txtSeriesThickness.getValue( );
		double dblPoints = getContext( ).getUIServiceProvider( )
				.getConvertedValue( dblCurrent,
						LiteralHelper.unitsOfMeasurementSet.getNameByDisplayName( cmbUnits.getText( ) ),
						UnitsOfMeasurement.POINTS_LITERAL.getName( ) );
		getChart( ).setSeriesThickness( dblPoints );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.
	 * Event)
	 */
	public void handleEvent( Event event )
	{
		if ( event.widget.equals( txtDescription ) )
		{
			if ( getChart( ).getDescription( ) != null )
			{
				getChart( ).getDescription( )
						.setValue( txtDescription.getText( ) );
			}
			else
			{
				org.eclipse.birt.chart.model.attribute.Text description = TextImpl.create( txtDescription.getText( ) );
				getChart( ).setDescription( description );
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse
	 * .swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected( SelectionEvent e )
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt
	 * .events.SelectionEvent)
	 */
	public void widgetSelected( SelectionEvent e )
	{
		if ( e.getSource( ).equals( cmbUnits ) )
		{
			getChart( ).setUnits( LiteralHelper.unitsOfMeasurementSet.getNameByDisplayName( cmbUnits.getText( ) ) );
			if ( cmbUnits.getSelectionIndex( ) != 0
					&& !btnSeriesThickness.getSelection( ) )
			{
				txtSeriesThickness.setValue( recalculateUnitDependentValues( txtSeriesThickness.getValue( ) ) );
			}
			// Update the Units for the Insets in Title properties
			lblSeriesThickness.setText( new MessageFormat( Messages.getString( "GeneralSheetImpl.Lbl.SeriesWidth" ) ).format( new Object[]{ //$NON-NLS-1$
				LiteralHelper.unitsOfMeasurementSet.getDisplayNameByName( getUnits( ) )
			} ) );
			sOldUnits = getUnits( );
		}
		else if ( e.getSource( ).equals( iscColumnCount ) )
		{
			getChart( ).setGridColumnCount( iscColumnCount.getSelection( ) );
		}
		else if ( e.getSource( ).equals( txtUnitSpacing ) )
		{
			( (ChartWithAxes) getChart( ) ).setUnitSpacing( txtUnitSpacing.getSelection( ) );
		}
		else if ( e.widget == btnSeriesThickness )
		{
			if ( btnSeriesThickness.getSelection( ) )
			{
				getChart( ).unsetSeriesThickness( );
			}
			else
			{
				updateToSeriesThickness( );
			}
			txtSeriesThickness.setEnabled( !btnSeriesThickness.getSelection( ) );
		}
		else if ( e.widget == btnColumnCount )
		{
			if ( btnColumnCount.getSelection( ) )
			{
				getChart( ).unsetGridColumnCount( );
			}
			else
			{
				getChart( ).setGridColumnCount( iscColumnCount.getSelection( ) );
			}
			iscColumnCount.setEnabled( !btnColumnCount.getSelection( ) );
		}
		else if ( e.widget == btnUnitSpacing )
		{
			if ( btnUnitSpacing.getSelection( ) )
			{
				( (ChartWithAxes) getChart( ) ).unsetUnitSpacing( );
			}
			else
			{
				( (ChartWithAxes) getChart( ) ).setUnitSpacing( txtUnitSpacing.getSelection( ) );
			}
			txtUnitSpacing.setEnabled( !btnUnitSpacing.getSelection( ) );
		}
	}

}