package orego.patterns;

/** A pattern that only applies when a specific color is to play. */
public class ColorSpecificPattern extends SimplePattern {

	/** Color to play when this pattern matches. */
	private int colorToPlay;
	
	/** @see orego.patterns.Pattern#setColors */
	public ColorSpecificPattern(String specification, int colorToPlay) {
		super(specification);
		this.colorToPlay = colorToPlay;
	}
	
	@Override
	public boolean matches(char pattern) {
		setColors(getSpecification(), colorToPlay);
		if (countsAsInAnyOrientation(pattern)) {
			return true;
		}
		return false;
	}

}
