package d.renderer.tetrahedron;

import java.awt.Color;

/**
 * Hello world!
 *
 */
public class Triangle 
{

	Axis a1;
	Axis a2;
	Axis a3;
	Color color;
	
	public Triangle(Axis a1, Axis a2, Axis a3, Color color){
		this.a1=a1;
		this.a2=a2;
		this.a3=a3;
		this.color = color;
	}
}
