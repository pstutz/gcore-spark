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

package algebra.trees

import algebra.expressions._
import algebra.operators.{CondMatchClause, SimpleMatchClause}
import algebra.types.{GraphPattern, NamedGraph, Vertex}
import org.scalatest.{BeforeAndAfterAll, FunSuite}

class MapBindingToGraphTest extends FunSuite with BeforeAndAfterAll with TestGraphWrapper {

  private val emptyObjPattern: ObjectPattern = ObjectPattern(True, True)
  private val labeledObjPattern: ObjectPattern =
    ObjectPattern(
      labelsPred = ConjunctLabels(And(DisjunctLabels(Seq(Label("Country"))), True)),
      propsPred = True)

  private val namedCatsGraph: NamedGraph = NamedGraph("cats graph")

  private val simpleMatchClause: SimpleMatchClause =
    SimpleMatchClause(
      graphPattern =
        GraphPattern(Seq(Vertex(Reference("food"), emptyObjPattern))),
      graph = namedCatsGraph)

  private val ambiguousExists: Exists =
    Exists(
      GraphPattern(Seq(Vertex(Reference("v"), emptyObjPattern))))

  private val labeledExists: Exists =
    Exists(
      GraphPattern(Seq(Vertex(Reference("ctry"), labeledObjPattern))))

  private val rewriter: MapBindingToGraph = MapBindingToGraph(AlgebraContext(catalog))

  override def beforeAll(): Unit = {
    super.beforeAll()
    catalog.registerGraph(catsGraph)
  }

  test("Bindings from SimpleMatchClause") {
    val expected = Map(Reference("food") -> namedCatsGraph)
    val actual = rewriter.mapBindingToGraph(simpleMatchClause)
    assert(actual == expected)
  }

  test("Bindings based on labels in Exists") {
    val expected = Map(Reference("ctry") -> namedCatsGraph)
    val actual = rewriter.mapBindingToGraph(labeledExists)
    assert(actual == expected)
  }

  // TODO: Un-ignore once property unrolling is allowed in the object pattern and also add it to
  // the all tests combined.
  ignore("Bindings based on properties in Exists") {
    val propertyObjPattern: ObjectPattern =
      ObjectPattern(
        labelsPred = True,
        propsPred = WithProps(And(PropertyKey("weight"), True)))
    val propertyExists: Exists =
      Exists(
        GraphPattern(Seq(Vertex(Reference("cat"), propertyObjPattern))))
    val expected = Map(Reference("cat") -> namedCatsGraph)
    val actual = rewriter.mapBindingToGraph(propertyExists)
    assert(actual == expected)
  }

  test("Ambiguous binding not added to map") {
    val actual = rewriter.mapBindingToGraph(ambiguousExists)
    assert(actual.isEmpty)
  }

  test("All tests combined") {
    val matchPred =
      And(
        labeledExists,
        And(
          True,
          ambiguousExists)
      )
    val condMatchClause = CondMatchClause(Seq(simpleMatchClause), matchPred)
    val expected =
      Map(
        Reference("food") -> namedCatsGraph,
        Reference("ctry") -> namedCatsGraph)
    val actual = rewriter.mapBindingToGraph(condMatchClause)
    assert(actual == expected)
  }
}
