package software.amazon.ce.costcategory

import software.amazon.awssdk.services.costexplorer.model.CostCategory
import software.amazon.awssdk.services.costexplorer.model.DescribeCostCategoryDefinitionRequest
import software.amazon.awssdk.services.costexplorer.model.DescribeCostCategoryDefinitionResponse
import software.amazon.cloudformation.proxy.OperationStatus

import static software.amazon.ce.costcategory.Fixtures.*

class ReadHandlerTest extends HandlerSpecification {

    def handler = new ReadHandler(ceClient)

    def "Test: ReadHandler.handleRequest"() {
        given:
        def describeResponse = DescribeCostCategoryDefinitionResponse.builder()
            .costCategory(CostCategory.builder()
                    .costCategoryArn(COST_CATEGORY_ARN)
                    .name(COST_CATEGORY_NAME)
                    .effectiveStart(COST_CATEGORY_EFFECTIVE_START)
                    .ruleVersion(RULE_VERSION)
                    .rules([ RULE_DIMENSION ])
                    .splitChargeRules([ SPLIT_CHARGE_RULE_EVEN ])
                    .defaultValue(COST_CATEGORY_DEFAULT_VALUE)
                    .build()
            ).build()

        def model = ResourceModel.builder()
            .arn(COST_CATEGORY_ARN)
            .build()

        when:
        def event = handler.handleRequest(proxy, request, callbackContext, logger)

        then:
        1 * request.getDesiredResourceState() >> model
        1 * proxy.injectCredentialsAndInvokeV2(*_) >> { DescribeCostCategoryDefinitionRequest describeRequest, _ ->
            assert describeRequest.costCategoryArn() == model.arn
            describeResponse
        }

        model.name == COST_CATEGORY_NAME
        model.effectiveStart == COST_CATEGORY_EFFECTIVE_START
        model.ruleVersion == RULE_VERSION
        model.rules == "[ ${JSON_RULE_DIMENSION} ]"
        model.splitChargeRules == "[ ${JSON_SPLIT_CHARGE_RULE_EVEN} ]"
        model.defaultValue == COST_CATEGORY_DEFAULT_VALUE

        event.resourceModel == model
        event.status == OperationStatus.SUCCESS
    }
}
