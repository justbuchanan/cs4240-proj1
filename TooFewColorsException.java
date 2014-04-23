//	thrown by InterferenceGraph.color() when there aren't enough colors to go around
public class TooFewColorsException extends Exception {
    public TooFewColorsException(String message) {
        super(message);
    }
}
