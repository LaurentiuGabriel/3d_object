package d.renderer.tetrahedron;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for simple App.
 */
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import org.testng.annotations.Test;

public class AppTest {
	@Test
	public void setUpScreen() {
		// 1. Create the frame.
		JFrame frame = new JFrame();
		Container pane = frame.getContentPane();
		// 2. Optional: What happens when the frame closes?
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// 3. Create components and put them in the frame.
		JSlider headingSlider = new JSlider(0, 360, 180);
		JSlider pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);

		pane.add(headingSlider, BorderLayout.SOUTH);
		pane.add(pitchSlider, BorderLayout.EAST);
		// 4. Size the frame.
		frame.setSize(400, 400);
		// 5. Create container to store the 2d graphics
		JPanel renderPanel = new JPanel() {
			public void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setColor(Color.BLACK);
				g2.fillRect(0, 0, getWidth(), getHeight());
				List<Triangle> tetrahedron = new ArrayList<>();
				tetrahedron.add(new Triangle(new Axis(100, 100, 100), new Axis(-100, -100, 100),
						new Axis(-100, 100, -100), Color.WHITE));
				tetrahedron.add(new Triangle(new Axis(100, 100, 100), new Axis(-100, -100, 100),
						new Axis(100, -100, -100), Color.CYAN));
				tetrahedron.add(new Triangle(new Axis(-100, 100, -100), new Axis(100, -100, -100),
						new Axis(100, 100, 100), Color.GREEN));
				tetrahedron.add(new Triangle(new Axis(-100, 100, -100), new Axis(100, -100, -100),
						new Axis(-100, -100, 100), Color.MAGENTA));

				for (int i = 0; i < 4; i++) {
					tetrahedron = inflate(tetrahedron);
				}

				double heading = Math.toRadians(headingSlider.getValue());
				Matrix headingTransform = new Matrix(new double[] { Math.cos(heading), 0, -Math.sin(heading), 0, 1, 0,
						Math.sin(heading), 0, Math.cos(heading) });
				double pitch = Math.toRadians(pitchSlider.getValue());
				Matrix pitchTransform = new Matrix(new double[] { 1, 0, 0, 0, Math.cos(pitch), Math.sin(pitch), 0,
						-Math.sin(pitch), Math.cos(pitch) });
				Matrix transform = headingTransform.multiply(pitchTransform);

				BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

				double[] zBuffer = new double[img.getWidth() * img.getHeight()];
				// initialize array with extremely far away depths
				for (int q = 0; q < zBuffer.length; q++) {
					zBuffer[q] = Double.NEGATIVE_INFINITY;
				}

				for (Triangle t : tetrahedron) {
					Axis v1 = transform.transform(t.a1);
					v1.x += getWidth() / 2;
					v1.y += getHeight() / 2;
					Axis v2 = transform.transform(t.a2);
					v2.x += getWidth() / 2;
					v2.y += getHeight() / 2;
					Axis v3 = transform.transform(t.a3);
					v3.x += getWidth() / 2;
					v3.y += getHeight() / 2;

					Axis ab = new Axis(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z);
					Axis ac = new Axis(v3.x - v1.x, v3.y - v1.y, v3.z - v1.z);
					Axis norm = new Axis(ab.y * ac.z - ab.z * ac.y, ab.z * ac.x - ab.x * ac.z,
							ab.x * ac.y - ab.y * ac.x);
					double normalLength = Math.sqrt(norm.x * norm.x + norm.y * norm.y + norm.z * norm.z);
					norm.x /= normalLength;
					norm.y /= normalLength;
					norm.z /= normalLength;

					double angleCos = Math.abs(norm.z);

					int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
					int maxX = (int) Math.min(img.getWidth() - 1, Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));
					int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
					int maxY = (int) Math.min(img.getHeight() - 1, Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));

					double triangleArea = (v1.y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - v1.x);

					for (int y = minY; y <= maxY; y++) {
						for (int x = minX; x <= maxX; x++) {
							double b1 = ((y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - x)) / triangleArea;
							double b2 = ((y - v1.y) * (v3.x - v1.x) + (v3.y - v1.y) * (v1.x - x)) / triangleArea;
							double b3 = ((y - v2.y) * (v1.x - v2.x) + (v1.y - v2.y) * (v2.x - x)) / triangleArea;
							if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
								double depth = b1 * v1.z + b2 * v2.z + b3 * v3.z;
								int zIndex = y * img.getWidth() + x;
								if (zBuffer[zIndex] < depth) {
									img.setRGB(x, y, getShade(t.color, angleCos).getRGB());
									zBuffer[zIndex] = depth;
								}
							}
						}
					}

				}

				g2.drawImage(img, 0, 0, null);
			}
		};
		// 6. Add the renderPanel

		headingSlider.addChangeListener(e -> renderPanel.repaint());
		pitchSlider.addChangeListener(e -> renderPanel.repaint());

		// 7. Show it.
		pane.add(renderPanel, BorderLayout.CENTER);
		frame.setVisible(true);
	}

	// 8. Inflate triangles
	public static List<Triangle> inflate(List<Triangle> trList) {
		List<Triangle> result = new ArrayList<>();
		for (Triangle t : trList) {
			Axis m1 = new Axis((t.a1.x + t.a2.x) / 2, (t.a1.y + t.a2.y) / 2, (t.a1.z + t.a2.z) / 2);
			Axis m2 = new Axis((t.a2.x + t.a3.x) / 2, (t.a2.y + t.a3.y) / 2, (t.a2.z + t.a3.z) / 2);
			Axis m3 = new Axis((t.a1.x + t.a3.x) / 2, (t.a1.y + t.a3.y) / 2, (t.a1.z + t.a3.z) / 2);
			result.add(new Triangle(t.a1, m1, m3, t.color));
			result.add(new Triangle(t.a2, m1, m2, t.color));
			result.add(new Triangle(t.a3, m2, m3, t.color));
			result.add(new Triangle(m1, m2, m3, t.color));
		}
		for (Triangle t : result) {
			for (Axis v : new Axis[] { t.a1, t.a2, t.a3 }) {
				double l = Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z) / Math.sqrt(30000);
				v.x /= l;
				v.y /= l;
				v.z /= l;
			}
		}
		return result;
	}

	public static Color getShade(Color color, double shade) {
		double redLinear = Math.pow(color.getRed(), 2.4) * shade;
		double greenLinear = Math.pow(color.getGreen(), 2.4) * shade;
		double blueLinear = Math.pow(color.getBlue(), 2.4) * shade;

		int red = (int) Math.pow(redLinear, 1 / 2.4);
		int green = (int) Math.pow(greenLinear, 1 / 2.4);
		int blue = (int) Math.pow(blueLinear, 1 / 2.4);

		return new Color(red, green, blue);
	}
}
