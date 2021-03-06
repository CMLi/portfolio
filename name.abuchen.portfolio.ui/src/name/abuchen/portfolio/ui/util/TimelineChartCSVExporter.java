package name.abuchen.portfolio.ui.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import name.abuchen.portfolio.ui.Messages;

import org.apache.commons.csv.CSVPrinter;
import org.eclipse.swt.widgets.Control;
import org.swtchart.ISeries;

public class TimelineChartCSVExporter extends AbstractCSVExporter
{
    private final TimelineChart chart;

    private Set<String> discontinousSeries = new HashSet<String>();

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
    private NumberFormat valueFormat = new DecimalFormat("#,##0.00"); //$NON-NLS-1$

    public TimelineChartCSVExporter(TimelineChart viewer)
    {
        this.chart = viewer;
    }

    public void setDateFormat(DateFormat dateFormat)
    {
        this.dateFormat = dateFormat;
    }

    public void setValueFormat(NumberFormat valueFormat)
    {
        this.valueFormat = valueFormat;
    }

    public void addDiscontinousSeries(String seriesId)
    {
        discontinousSeries.add(seriesId);
    }

    @Override
    protected Control getControl()
    {
        return chart;
    }

    @Override
    protected void writeToFile(File file) throws IOException
    {
        Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8")); //$NON-NLS-1$

        try
        {
            CSVPrinter printer = new CSVPrinter(writer);
            printer.setStrategy(STRATEGY);

            ISeries[] series = chart.getSeriesSet().getSeries();

            // write header
            printer.print(Messages.ColumnDate);
            for (ISeries s : series)
                printer.print(s.getId());
            printer.println();

            // write body
            Date[] dateSeries = series[0].getXDateSeries();

            SeriesAdapter[] adapters = new SeriesAdapter[series.length];
            for (int ii = 0; ii < series.length; ii++)
            {
                if (discontinousSeries.contains(series[ii].getId()))
                    adapters[ii] = new DiscontinousAdapter(series[ii]);
                else
                    adapters[ii] = new DefaultAdapter(series[ii]);
            }

            for (int line = 0; line < dateSeries.length; line++)
            {
                printer.print(dateFormat.format(dateSeries[line]));

                for (int col = 0; col < adapters.length; col++)
                    printer.print(adapters[col].format(dateSeries[line], line));

                printer.println();
            }
        }
        finally
        {
            writer.close();
        }
    }

    private interface SeriesAdapter
    {
        public String format(Date date, int line);
    }

    private class DefaultAdapter implements SeriesAdapter
    {
        private double[] values;

        public DefaultAdapter(ISeries series)
        {
            this.values = series.getYSeries();
        }

        @Override
        public String format(Date date, int line)
        {
            return valueFormat.format(values[line]);
        }
    }

    private class DiscontinousAdapter implements SeriesAdapter
    {
        private Date[] dates;
        private double[] values;

        private int next = 0;

        public DiscontinousAdapter(ISeries series)
        {
            this.dates = series.getXDateSeries();
            this.values = series.getYSeries();
        }

        @Override
        public String format(Date date, int line)
        {
            if (next >= dates.length)
                return ""; //$NON-NLS-1$

            if (date.getTime() != dates[next].getTime())
                return ""; //$NON-NLS-1$

            return valueFormat.format(values[next++]);
        }
    }
}
