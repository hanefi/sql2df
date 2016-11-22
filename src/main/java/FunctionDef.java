package main;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for in memory function representation.
 * Function declarations are read from the files and stored in the instances of this class.
 */
public class FunctionDef {
    /**
     * Name of the function
     */
    public String name;
    /**
     * Return type of the function
     */
    public String returnType;
    /**
     * Ordered parameter list of the function
     */
    public List<String> parameters;

    /**
     * Creates a function definition.
     * @param name Name of the function
     * @param returnType Return type of the function
     * @param parameters Ordered parameter list of the function
     */
    public FunctionDef(String name, String returnType, List<String> parameters) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = new ArrayList<>(parameters);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FunctionDef that = (FunctionDef) o;

        if (!name.equals(that.name)) return false;
        if (!returnType.equals(that.returnType)) return false;
        return parameters.equals(that.parameters);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + returnType.hashCode();
        result = 31 * result + parameters.hashCode();
        return result;
    }
    
    @Override
    public String toString(){
    	return "Function: "+name+" "+returnType+" "+parameters;
    }
}
