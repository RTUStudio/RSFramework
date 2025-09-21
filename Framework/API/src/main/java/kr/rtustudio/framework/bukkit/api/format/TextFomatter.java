package kr.rtustudio.framework.bukkit.api.format;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TextFomatter {

    // @Inject
    // private static RSFramework framework;
    //
    // private static final MessageConfiguration message =
    // framework.getConfigurations().getMessage();
    //
    // public static DataNameType checkType(List<String> list, String name) {
    // if (name.matches("[^ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z0-9_]*$")) {
    // return DataNameType.ALL;
    // } else if (!name.matches("^[ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z0-9_]*$")) {
    // return DataNameType.WRONG;
    // }
    // if (name.length() > 15) {
    // return DataNameType.LONG;
    // }
    // if (Collections.frequency(list, name) >= 1) {
    // return DataNameType.EXIST;
    // }
    // return DataNameType.PASS;
    // }
    //
    // public static DataNameType checkType(Player adapter, List<String> list,
    // String name) {
    // Audience audience = framework.getAdventure().adapter(adapter);
    // DataNameType dataNameType = checkType(list, name);
    // switch (dataNameType) {
    // case ALL ->
    // audience.sendMessage(ComponentFormatter.parse(message.get("dataNameType.all")));
    // case WRONG ->
    // audience.sendMessage(ComponentFormatter.parse(message.get("dataNameType.wrong")));
    // case LONG ->
    // audience.sendMessage(ComponentFormatter.parse(message.get("dataNameType.long")));
    // case EXIST ->
    // audience.sendMessage(ComponentFormatter.parse(message.get("dataNameType.exist")));
    // default -> audience.sendMessage(ComponentFormatter.parse(message.get("")));
    // }
    // return dataNameType;
    // }

    public enum DataNameType {
        /** 이름 전체가 모두 잘못된 문자 */
        ALL,
        /** 이름에 잘못된 문자를 포함 */
        WRONG,
        /** 이름의 길이가 10자 초과 */
        LONG,
        /** 이미 존재하는 이름 */
        EXIST,
        /** 문제 없음 */
        PASS
    }
}
