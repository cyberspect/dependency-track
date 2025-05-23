/*
 * This file is part of Dependency-Track.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) OWASP Foundation. All Rights Reserved.
 */
package org.dependencytrack.policy;

import alpine.common.logging.Logger;
import org.dependencytrack.model.Component;
import org.dependencytrack.model.License;
import org.dependencytrack.model.LicenseGroup;
import org.dependencytrack.model.Policy;
import org.dependencytrack.model.PolicyCondition;
import org.dependencytrack.parser.spdx.expression.model.SpdxExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * Evaluates a components resolved license against a policy.
 *
 * @author Steve Springett
 * @since 4.0.0
 */
public class LicensePolicyEvaluator extends AbstractPolicyEvaluator {

    private static final Logger LOGGER = Logger.getLogger(LicensePolicyEvaluator.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public PolicyCondition.Subject supportedSubject() {
        return PolicyCondition.Subject.LICENSE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PolicyConditionViolation> evaluate(final Policy policy, final Component component) {
        final List<PolicyConditionViolation> violations = new ArrayList<>();

        final List<PolicyCondition> policyConditions = super.extractSupportedConditions(policy);
        if (policyConditions.isEmpty()) {
            return violations;
        }

        // use spdx expression checking logic from the license group policy evaluator
        final SpdxExpression expression = LicenseGroupPolicyEvaluator.getSpdxExpressionFromComponent(component);

        boolean allPoliciesViolated = true;
        for (final PolicyCondition condition: super.extractSupportedConditions(policy)) {
            LOGGER.debug("Evaluating component (" + component.getUuid() + ") against policy condition (" + condition.getUuid() + ")");

            LicenseGroup licenseGroup = null;
            // lg will stay null if we are checking for "unresolved"
            if (!condition.getValue().equals("unresolved")) {
                License conditionLicense = qm.getObjectByUuid(License.class, condition.getValue());
                licenseGroup = LicenseGroupPolicyEvaluator.getTemporaryLicenseGroupForLicense(conditionLicense);
            }

            boolean addedViolation = LicenseGroupPolicyEvaluator.evaluateCondition(qm, condition, expression,
                    licenseGroup, component, violations);
            if (addedViolation == false) {
                allPoliciesViolated = false;
            }

        }
        return violations;
    }

}