package odoo.client.service;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.EMPTY_MAP;
import static java.util.Collections.emptyMap;

/**
 * @author nnvh
 */
public class OdooService {
    private final XmlRpcClient client = new XmlRpcClient();
    private final String url = "http://localhost:10017/";
    private final String db = "demo";
    private final String username = "admin";
    private final String password = "1234";

    private Integer uid;

    public XmlRpcClientConfigImpl buildCommonConfig() throws MalformedURLException {
        final XmlRpcClientConfigImpl common_config = new XmlRpcClientConfigImpl();
        common_config.setServerURL(new URL(String.format("%s/xmlrpc/2/common", url)));
        return common_config;
    }

    public Integer login() throws MalformedURLException, XmlRpcException {
        this.uid = (int) client.execute(this.buildCommonConfig(), "authenticate", Arrays.asList(db, username, password, emptyMap()));
        return this.uid;
    }

    public XmlRpcClient buildModel() throws MalformedURLException {
        final XmlRpcClient models = new XmlRpcClient() {
            {
                setConfig(new XmlRpcClientConfigImpl() {{
                    setServerURL(new URL(String.format("%s/xmlrpc/2/object", url)));
                }});
            }

        };
        return models;
    }

    public Object execute(String modelName, String methodName, List<Object> params, Map<Object, Object> keywords) throws MalformedURLException, XmlRpcException {
        return buildModel().execute("execute_kw", Arrays.asList(
                db, uid, password,
                modelName, methodName,
                Arrays.asList(params),
                keywords
        ));
    }

    public Object execute(String modelName, String methodName, List<Object> params) throws MalformedURLException, XmlRpcException {
        return buildModel().execute("execute_kw", Arrays.asList(
                db, uid, password,
                modelName, methodName,
                Arrays.asList(params),
                EMPTY_MAP
        ));
    }

    public Object execute(String modelName, String methodName, Map<Object, Object> keywords) throws MalformedURLException, XmlRpcException {
        return buildModel().execute("execute_kw", Arrays.asList(
                db, uid, password,
                modelName, methodName,
                EMPTY_LIST,
                keywords
        ));
    }

    public List<Integer> search(String modelName, List<Object> params, Map<Object, Object> keywords) throws MalformedURLException, XmlRpcException {
        Object[] result = (Object[]) execute(modelName, "search", params, keywords);
        return Stream.of(result).mapToInt(Integer.class::cast).boxed().collect(Collectors.toList());
    }

    public List<Integer> search(String modelName, List<Object> params) throws MalformedURLException, XmlRpcException {
        Object[] result = (Object[]) execute(modelName, "search", params);
        return Stream.of(result).mapToInt(Integer.class::cast).boxed().collect(Collectors.toList());
    }

    public Integer searchCount(String modelName, List<Object> params) throws MalformedURLException, XmlRpcException {
        return (Integer) execute(modelName, "search_count", params);
    }

    public List<Object> read(String modelName, List<Integer> ids, Map<Object, Object> keywords) throws MalformedURLException, XmlRpcException {
        return Arrays.asList((Object[]) execute(modelName, "read", Arrays.asList(ids.toArray()), keywords));
    }

    public List<Object> read(String modelName, List<Integer> ids) throws MalformedURLException, XmlRpcException {
        return Arrays.asList((Object[]) execute(modelName, "read", Arrays.asList(ids.toArray())));
    }

    public List<Object> getFields(String modelName, Map<Object, Object> keywords) throws MalformedURLException, XmlRpcException {
        return Arrays.asList((Object[]) execute(modelName, "fields_get", keywords));
    }

    public Object getFields(String modelName) throws MalformedURLException, XmlRpcException {
        return getFields(modelName, new HashMap() {{
            put("attributes", Arrays.asList("string", "help", "type"));
        }});
    }

    public List<Object> searchRead(String modelName, List<Object> params, Map<Object, Object> keywords) throws MalformedURLException, XmlRpcException {
        return Arrays.asList((Object[]) execute(modelName, "search_read", params, keywords));
    }

    public List<Object> searchRead(String modelName, List<Object> params) throws MalformedURLException, XmlRpcException {
        return Arrays.asList((Object[]) execute(modelName, "search_read", params));
    }

    public Integer create(String modelName, Map<Object, Object> fields) throws MalformedURLException, XmlRpcException {
        Object[] res = (Object[]) execute(modelName, "create", Collections.singletonList(fields));
        return (Integer) res[0];
    }

    public Object update(String modelName, List<Object> params) throws MalformedURLException, XmlRpcException {
        return execute(modelName, "write", params);
    }

    public Object delete(String modelName, List<Object> ids) throws MalformedURLException, XmlRpcException {
        return execute(modelName, "unlink", ids);
    }

}
