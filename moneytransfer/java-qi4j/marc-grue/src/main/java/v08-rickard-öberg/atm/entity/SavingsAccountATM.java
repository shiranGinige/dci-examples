/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package v08.atm.entity;

import v07.context.TransferMoneyContext;
import v08.domain.entity.SavingsAccountEntity;

/**
 * A savings account can be a source account, but not a destination account
 */
public interface SavingsAccountATM
      extends SavingsAccountEntity,
      // Roles
      TransferMoneyContext.SourceAccountRole
{}