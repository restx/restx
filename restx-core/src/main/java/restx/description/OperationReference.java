package restx.description;

/**
 * Date: 11/11/13
 * Time: 21:35
 */
public class OperationReference {
    public String apiDocName; // reference of the api doc in which this operation appears, i.e. the its router name
    public String httpMethod;
    public String path;
    public String requestClass;
    public String responseClass;




    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OperationReference that = (OperationReference) o;

        if (apiDocName != null ? !apiDocName.equals(that.apiDocName) : that.apiDocName != null) return false;
        if (httpMethod != null ? !httpMethod.equals(that.httpMethod) : that.httpMethod != null) return false;
        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        if (requestClass != null ? !requestClass.equals(that.requestClass) : that.requestClass != null) return false;
        if (responseClass != null ? !responseClass.equals(that.responseClass) : that.responseClass != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = apiDocName != null ? apiDocName.hashCode() : 0;
        result = 31 * result + (httpMethod != null ? httpMethod.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (requestClass != null ? requestClass.hashCode() : 0);
        result = 31 * result + (responseClass != null ? responseClass.hashCode() : 0);
        return result;
    }
}
