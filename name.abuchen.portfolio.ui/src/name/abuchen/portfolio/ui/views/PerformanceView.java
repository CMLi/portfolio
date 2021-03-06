package name.abuchen.portfolio.ui.views;

import java.util.Calendar;

import name.abuchen.portfolio.model.Account;
import name.abuchen.portfolio.model.Transaction;
import name.abuchen.portfolio.model.Values;
import name.abuchen.portfolio.snapshot.ClientPerformanceSnapshot;
import name.abuchen.portfolio.ui.Messages;
import name.abuchen.portfolio.ui.PortfolioPlugin;
import name.abuchen.portfolio.ui.util.ColumnViewerSorter;
import name.abuchen.portfolio.ui.util.ShowHideColumnHelper;
import name.abuchen.portfolio.ui.util.ShowHideColumnHelper.Column;
import name.abuchen.portfolio.ui.util.SimpleListContentProvider;
import name.abuchen.portfolio.ui.util.ViewerHelper;
import name.abuchen.portfolio.util.Dates;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class PerformanceView extends AbstractHistoricView
{
    private TreeViewer calculation;
    private StatementOfAssetsViewer snapshotStart;
    private StatementOfAssetsViewer snapshotEnd;
    private TableViewer earnings;

    public PerformanceView()
    {
        super(0);
    }

    @Override
    protected String getTitle()
    {
        return Messages.LabelPerformanceCalculation;
    }

    @Override
    protected void reportingPeriodUpdated()
    {
        Calendar startDate = Calendar.getInstance();
        startDate.setTime(Dates.today());
        startDate.add(Calendar.YEAR, -getReportingYears());
        startDate.set(Calendar.DAY_OF_MONTH, 1);
        startDate.add(Calendar.DATE, -1);

        ClientPerformanceSnapshot snapshot = new ClientPerformanceSnapshot(getClient(), startDate.getTime(),
                        Dates.today());

        try
        {
            calculation.getTree().setRedraw(false);
            calculation.setInput(snapshot);
            calculation.expandAll();
            ViewerHelper.pack(calculation);
            calculation.getTree().getParent().layout();
        }
        finally
        {
            calculation.getTree().setRedraw(true);
        }

        snapshotStart.setInput(snapshot.getStartClientSnapshot());
        snapshotStart.pack();
        snapshotEnd.setInput(snapshot.getEndClientSnapshot());
        snapshotEnd.pack();

        earnings.setInput(snapshot.getEarnings());
    }

    @Override
    protected Control createBody(Composite parent)
    {
        // result tabs
        CTabFolder folder = new CTabFolder(parent, SWT.BORDER);

        createCalculationItem(folder, Messages.PerformanceTabCalculation);
        snapshotStart = createStatementOfAssetsItem(folder, Messages.PerformanceTabAssetsAtStart);
        snapshotEnd = createStatementOfAssetsItem(folder, Messages.PerformanceTabAssetsAtEnd);
        createEarningsItem(folder, Messages.PerformanceTabEarnings);

        folder.setSelection(0);

        reportingPeriodUpdated();

        return folder;
    }

    private StatementOfAssetsViewer createStatementOfAssetsItem(CTabFolder folder, String title)
    {
        StatementOfAssetsViewer viewer = new StatementOfAssetsViewer(folder, getClient());
        CTabItem item = new CTabItem(folder, SWT.NONE);
        item.setText(title);
        item.setControl(viewer.getControl());

        return viewer;
    }

    private void createCalculationItem(CTabFolder folder, String title)
    {
        Composite container = new Composite(folder, SWT.NONE);
        TreeColumnLayout layout = new TreeColumnLayout();
        container.setLayout(layout);

        calculation = new TreeViewer(container, SWT.FULL_SELECTION);

        TreeViewerColumn column = new TreeViewerColumn(calculation, SWT.NONE);
        column.getColumn().setText(Messages.ColumnLable);
        layout.setColumnData(column.getColumn(), new ColumnPixelData(350));

        column = new TreeViewerColumn(calculation, SWT.RIGHT);
        column.getColumn().setText(Messages.ColumnValue);
        layout.setColumnData(column.getColumn(), new ColumnPixelData(80));

        calculation.getTree().setHeaderVisible(true);
        calculation.getTree().setLinesVisible(true);

        calculation.setLabelProvider(new PerformanceLabelProvider());
        calculation.setContentProvider(new PerformanceContentProvider());

        CTabItem item = new CTabItem(folder, SWT.NONE);
        item.setText(title);
        item.setControl(container);
    }

    private void createEarningsItem(CTabFolder folder, String title)
    {
        Composite container = new Composite(folder, SWT.NONE);
        TableColumnLayout layout = new TableColumnLayout();
        container.setLayout(layout);

        earnings = new TableViewer(container, SWT.FULL_SELECTION);

        ShowHideColumnHelper support = new ShowHideColumnHelper(PerformanceView.class.getSimpleName() + "@earnings", //$NON-NLS-1$
                        earnings, layout);

        Column column = new Column(Messages.ColumnDate, SWT.None, 80);
        column.setLabelProvider(new ColumnLabelProvider()
        {
            @Override
            public String getText(Object element)
            {
                return Values.Date.format(((Transaction) element).getDate());
            }
        });
        column.setSorter(ColumnViewerSorter.create(Transaction.class, "date"), SWT.UP); //$NON-NLS-1$
        support.addColumn(column);

        column = new Column(Messages.ColumnAmount, SWT.RIGHT, 80);
        column.setLabelProvider(new ColumnLabelProvider()
        {
            @Override
            public String getText(Object element)
            {
                return Values.Amount.format(((Transaction) element).getAmount());
            }
        });
        column.setSorter(ColumnViewerSorter.create(Transaction.class, "amount")); //$NON-NLS-1$
        support.addColumn(column);

        column = new Column(Messages.ColumnSource, SWT.LEFT, 400);
        column.setLabelProvider(new ColumnLabelProvider()
        {
            @Override
            public String getText(Object element)
            {
                Transaction transaction = (Transaction) element;
                if (transaction.getSecurity() != null)
                    return transaction.getSecurity().getName();

                for (Account account : getClient().getAccounts())
                {
                    if (account.getTransactions().contains(transaction))
                        return account.getName();
                }

                return null;
            }

            @Override
            public Image getImage(Object element)
            {
                Transaction transaction = (Transaction) element;
                return PortfolioPlugin.image(transaction.getSecurity() != null ? PortfolioPlugin.IMG_SECURITY
                                : PortfolioPlugin.IMG_ACCOUNT);
            }
        });
        column.setSorter(ColumnViewerSorter.create(Transaction.class, "security")); //$NON-NLS-1$
        support.addColumn(column);

        support.createColumns();

        earnings.getTable().setHeaderVisible(true);
        earnings.getTable().setLinesVisible(true);

        earnings.setContentProvider(new SimpleListContentProvider());

        CTabItem item = new CTabItem(folder, SWT.NONE);
        item.setText(title);
        item.setControl(container);
    }

    private static class PerformanceContentProvider implements ITreeContentProvider
    {
        private ClientPerformanceSnapshot.Category[] categories;

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
        {
            if (newInput == null)
            {
                this.categories = new ClientPerformanceSnapshot.Category[0];
            }
            else if (newInput instanceof ClientPerformanceSnapshot)
            {
                this.categories = ((ClientPerformanceSnapshot) newInput).getCategories().toArray(
                                new ClientPerformanceSnapshot.Category[0]);
            }
            else
            {
                throw new RuntimeException("Unsupported type: " + newInput.getClass().getName()); //$NON-NLS-1$
            }
        }

        public Object[] getElements(Object inputElement)
        {
            return this.categories;
        }

        public Object[] getChildren(Object parentElement)
        {
            if (parentElement instanceof ClientPerformanceSnapshot.Category)
                return ((ClientPerformanceSnapshot.Category) parentElement).getPositions().toArray(
                                new ClientPerformanceSnapshot.Position[0]);
            return null;
        }

        public Object getParent(Object element)
        {
            if (element instanceof ClientPerformanceSnapshot.Position)
            {
                for (ClientPerformanceSnapshot.Category c : categories)
                {
                    if (c.getPositions().contains(element))
                        return c;
                }

            }
            return null;
        }

        public boolean hasChildren(Object element)
        {
            return element instanceof ClientPerformanceSnapshot.Category
                            && !((ClientPerformanceSnapshot.Category) element).getPositions().isEmpty();
        }

        public void dispose()
        {}

    }

    private static class PerformanceLabelProvider extends LabelProvider implements ITableLabelProvider,
                    ITableFontProvider
    {
        private FontRegistry registry = new FontRegistry();

        public Image getColumnImage(Object element, int columnIndex)
        {
            if (columnIndex != 0)
                return null;

            if (element instanceof ClientPerformanceSnapshot.Category)
            {
                return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
            }
            else if (element instanceof ClientPerformanceSnapshot.Position)
            {
                ClientPerformanceSnapshot.Position position = (ClientPerformanceSnapshot.Position) element;
                return position.getSecurity() != null ? PortfolioPlugin.image(PortfolioPlugin.IMG_SECURITY) : null;
            }
            return null;
        }

        public String getColumnText(Object element, int columnIndex)
        {
            if (element instanceof ClientPerformanceSnapshot.Category)
            {
                ClientPerformanceSnapshot.Category cat = (ClientPerformanceSnapshot.Category) element;

                switch (columnIndex)
                {
                    case 0:
                        return cat.getLabel();
                    case 1:
                        return Values.Amount.format(cat.getValuation());
                }
            }
            else if (element instanceof ClientPerformanceSnapshot.Position)
            {
                ClientPerformanceSnapshot.Position pos = (ClientPerformanceSnapshot.Position) element;

                switch (columnIndex)
                {
                    case 0:
                        return pos.getLabel();
                    case 1:
                        return Values.Amount.format(pos.getValuation());
                }
            }
            return null;
        }

        @Override
        public Font getFont(Object element, int columnIndex)
        {
            if (element instanceof ClientPerformanceSnapshot.Category)
            {
                return registry.getBold(Display.getCurrent().getSystemFont().getFontData()[0].getName());
            }
            else
            {
                return null;
            }
        }
    }

}
