package algebra.trees

import algebra.expressions._
import algebra.operators.{CondMatchClause, SimpleMatchClause}
import algebra.types.{GraphPattern, NamedGraph, Vertex}
import org.scalatest.{BeforeAndAfterAll, FunSuite}

class MapBindingToGraphTest extends FunSuite with BeforeAndAfterAll with TestGraphWrapper {

  private val emptyObjPattern: ObjectPattern = ObjectPattern(True(), True())
  private val labeledObjPattern: ObjectPattern =
    ObjectPattern(
      labelsPred = WithLabels(And(HasLabel(Seq(Label("Country"))), True())),
      propsPred = True())
  private val propertyObjPattern: ObjectPattern =
    ObjectPattern(
      labelsPred = True(),
      propsPred = WithProps(And(PropertyKey("weight"), True())))

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

  private val propertyExists: Exists =
    Exists(
      GraphPattern(Seq(Vertex(Reference("cat"), propertyObjPattern))))

  private val rewriter: MapBindingToGraph = MapBindingToGraph(AlgebraContext(graphDb))

  override def beforeAll(): Unit = {
    super.beforeAll()
    graphDb.registerGraph(catsGraph)
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

  test("Bindings based on properties in Exists") {
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
          propertyExists,
          ambiguousExists)
      )
    val condMatchClause = CondMatchClause(Seq(simpleMatchClause), matchPred)
    val expected =
      Map(
        Reference("food") -> namedCatsGraph,
        Reference("ctry") -> namedCatsGraph,
        Reference("cat") -> namedCatsGraph)
    val actual = rewriter.mapBindingToGraph(condMatchClause)
    assert(actual == expected)
  }
}