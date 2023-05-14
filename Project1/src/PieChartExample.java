import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import javax.swing.JFrame;
import java.awt.Dimension;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import java.awt.Color;

public class PieChartExample extends JFrame {

    public PieChartExample() {
        // Create a dataset
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("One", (double)(43.2));
        dataset.setValue("Two", (double)(10.0));
        dataset.setValue("Three", (double)(27.5));
        dataset.setValue("Four", (double)(17.5));
        dataset.setValue("Five", (double)(11.0));
        
        // Create a chart
        JFreeChart chart = ChartFactory.createPieChart(
                "Pie Chart Example",  // chart title
                dataset,              // data
                true,                 // include legend
                true,                 // tooltips
                false                 // URLs
        );
        
        // Customize the appearance of the chart
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setExplodePercent("One", 0.20);
        plot.setSectionPaint("Two", new Color(0, 128, 0));
        plot.setLabelFont(plot.getLabelFont().deriveFont(14.0f));
        
        // Create a chart panel and set it to the content pane
        ChartPanel chartPanel = new ChartPanel(chart);
        JPanel contentPane = new JPanel();
        contentPane.setPreferredSize(new Dimension(500, 400));
        contentPane.add(chartPanel);
        setContentPane(contentPane);
    }

    public static void main(String[] args) {
        PieChartExample demo = new PieChartExample();
        demo.pack();
        demo.setVisible(true);
        demo.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
}