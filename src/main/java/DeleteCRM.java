import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;

public class DeleteCRM {

    public static final String appId = "FSAID_131b579";
    public static final String appSecret = "82d60d15e135497e869215dffc1ac9b2";
    public static final String permanentCode = "CF920E8280CAF54D4E32E73846C21BA1";

    public static final String loginUrl = "https://open.fxiaoke.com/cgi/corpAccessToken/get/V2";
    public static final String userUrl = "https://open.fxiaoke.com/cgi/user/getByMobile";

    public static final String queryUrl = "https://open.fxiaoke.com/cgi/crm/v2/data/query";
    public static final String invalidUrl = "https://open.fxiaoke.com/cgi/crm/v2/data/invalid";

    public static final String deleteUrl = "https://open.fxiaoke.com/cgi/crm/v2/data/delete";


    public static String login() {
        String jsonString = "{\"appId\":\"FSAID_131b579\",\"appSecret\":\"82d60d15e135497e869215dffc1ac9b2\",\"permanentCode\":\"CF920E8280CAF54D4E32E73846C21BA1\"}";

        HttpResponse request = HttpRequest.post(loginUrl)
                .header(Header.CONTENT_TYPE, "application/json")
                .body(jsonString)
                .timeout(30000).execute();
        return request.body();
    }

    public static String getUserId(String body) {
        HttpResponse request = HttpRequest.post(userUrl)
                .header(Header.CONTENT_TYPE, "application/json")
                .body(body)
                .timeout(30000).execute();
        return request.body();
    }

    public static String invalid(String body) {

        HttpResponse request = HttpRequest.post(invalidUrl)
                .header(Header.CONTENT_TYPE, "application/json")
                .body(body)
                .timeout(30000).execute();
        return request.body();
    }

    public static String delete(String body) {

        HttpResponse request = HttpRequest.post(deleteUrl)
                .header(Header.CONTENT_TYPE, "application/json")
                .body(body)
                .timeout(30000).execute();
        return request.body();
    }


    public static String getIds(String body) {
        HttpResponse request = HttpRequest.post(queryUrl)
                .header(Header.CONTENT_TYPE, "application/json")
                .body(body)
                .timeout(30000).execute();
        return request.body();
    }

    public static void main(String[] args) {
        JSONObject login = JSONObject.parseObject(login());
        String token = login.getString("corpAccessToken");
        System.out.println("corpAccessToken：" + token);
        String corpId = login.getString("corpId");
        System.out.println("corpId:" + corpId);
        String userBody = "{\"corpAccessToken\":\"" + token + "\",\"corpId\":\"" + corpId + "\",\"mobile\":\"18010672665\"}";
        JSONObject user = JSONObject.parseObject(getUserId(userBody));
        JSONArray userArray = user.getJSONArray("empList");
        JSONObject userC = (JSONObject) userArray.get(0);
        String userId = userC.getString("openUserId");
        System.out.println("userId:" + userId);
        String idsBody = "{\"corpAccessToken\":\"" + token + "\",\"corpId\":\"" + corpId + "\",\"currentOpenUserId\":\"" + userId + "\",\"data\":{\"dataObjectApiName\":\"JournalObj\",\"search_query_info\"" +
                ":{\"limit\":1,\"offset\":0,\"filters\":[{\"field_name\":\"create_time\",\"field_values\":[\"1677600000000\"],\"operator\":\"LTE\"}],\"orders\":[{\"fieldName\":\"journal_time\",\"isAsc\":false}]}}}";
        JSONObject ids = JSONObject.parseObject(getIds(idsBody));
        JSONObject data = ids.getJSONObject("data");
        JSONArray idsList = data.getJSONArray("dataList");
        if (idsList.isEmpty()) {
            System.out.println("已操作完成");
            return;
        }
//        ArrayList<String> idsRes = new ArrayList<>();

        System.out.println("操作的数据总量为：" + ids.size());

        for (int i = 0; i < ids.size(); i++) {
            JSONObject idObj = (JSONObject) idsList.get(i);
            String id = idObj.getString("_id");
//            idsRes.add(id.getString("_id"));


            String body = "{\"corpAccessToken\":\"" + token + "\",\"corpId\":\"" + corpId + "\",\"currentOpenUserId\":\"" + userId + "\"," +
                    "\"data\":{\"object_data_id\":\"" + id + "\",\"dataObjectApiName\":\"JournalObj\"}}";

            JSONObject invalidJson = JSONObject.parseObject(invalid(body));

            String bodyDelete = "{\"corpAccessToken\":\"" + token + "\",\"corpId\":\"" + corpId + "\",\"currentOpenUserId\":\"" + userId + "\"," +
                    "\"data\":{\"idList\":[\"" + id + "\"],\"dataObjectApiName\":\"JournalObj\"}}";

            JSONObject deleteJson = JSONObject.parseObject(delete(bodyDelete));
            if (deleteJson.getInteger("errorCode") == 0){
                System.out.println("操作数据：" + id + "成功");
            }else{
                System.out.println("操作数据失败：" + id  + "  原因为： " + deleteJson.getString("errorMessage"));
            }

        }


    }
}
