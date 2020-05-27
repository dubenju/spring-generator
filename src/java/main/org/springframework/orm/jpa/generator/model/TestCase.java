package org.springframework.orm.jpa.generator.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TestCase {
    /** 番号 */
    private String num;
    /** メソッド */
    private String methodName;
    /** 枝番 */
    private String numSub;
    /** 確認ケース */
    private String testCase;
    /** 検証ポイント */
    private String testPoint;
    /** 証明方法 */
    private String testMethod;
    /** 結果と補足 */
    private String testResult;
    /** 検証日 */
    private String testDate;
    /** 検証者 */
    private String tester;
    /** 備考 */
    private String other;
}
