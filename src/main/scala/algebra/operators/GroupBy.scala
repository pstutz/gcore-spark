/*
 * gcore-spark is the reference implementation of the G-CORE graph query
 * language by the Linked Data Benchmark Council (LDBC) - ldbcouncil.org
 *
 * The copyrights of the source code in this file belong to:
 * - CWI (www.cwi.nl), 2017-2018
 *
 * This software is released in open source under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package algebra.operators

import algebra.expressions.{AggregateExpression, AlgebraExpression, PropertySet, Reference}
import algebra.trees.AlgebraTreeNode

/**
  * The relational group by operation. The [[aggregateFunctions]] will create new properties for the
  * [[reference]] by using [[AlgebraExpression]]s over [[AggregateExpression]]s.
  *
  * For example, the following patterns will result in the respective parameters:
  * > CONSTRUCT (c) => [[reference]] = c
  *                    [[groupingAttributes]] = {c} // group by identity
  *                    [[aggregateFunctions]] = {}
  *
  * > CONSTRUCT (x) => [[reference]] = x
  *                    [[groupingAttributes]] = {} // nothing to group by, btable used as is
  *                    [[aggregateFunctions]] = {}
  *
  * > CONSTRUCT (x GROUP c.prop) => [[reference]] = x
  *                                 [[groupingAttributes]] = {c.prop}
  *                                 [[aggregateFunctions]] = {}
  *
  * > CONSTRUCT (x GROUP c.prop0 {prop1 := AVG(c.prop1)}) =>
  *                                 [[reference]] = x
  *                                 [[groupingAttributes]] = {c.prop}
  *                                 [[aggregateFunctions]] = {x.prop1 := AVG(c.prop1)}
  */
case class GroupBy(reference: Reference, // The entity for which we group
                   relation: RelationLike,
                   groupingAttributes: Seq[AlgebraTreeNode], // Reference or GroupDeclaration
                   aggregateFunctions: Seq[PropertySet],
                   having: Option[AlgebraExpression] = None)
  extends UnaryOperator(
    relation, bindingSet = Some(relation.getBindingSet ++ new BindingSet(reference))) {

  children = (reference +: relation +: groupingAttributes) ++ aggregateFunctions ++ having.toList

  /** Handy extractors of this operator's children. */
  def getRelation: AlgebraTreeNode = children(1)

  def getGroupingAttributes: Seq[AlgebraTreeNode] = children.slice(2, 2 + groupingAttributes.size)

  def getAggregateFunction: Seq[PropertySet] = aggregateFunctions

  def getHaving: Option[AlgebraExpression] = having
}
