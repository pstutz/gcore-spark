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

package common.trees

import org.scalatest.FunSuite

class TreeRewriterTest extends FunSuite with TestTreeWrapper {

  val fEven: PartialFunction[IntTree, IntTree] = {
    case node if node.value % 2 == 0 =>
      IntTree(value = node.children.map(_.value).sum + node.value / 2, descs = node.children)
  }

  val fOdd: PartialFunction[IntTree, IntTree] = {
    case node if node.value % 2 == 1 =>
      IntTree(value = node.children.map(_.value).sum + node.value, descs = node.children)
  }

  val f: PartialFunction[IntTree, IntTree] = fEven orElse fOdd

  test("topDownRewriter") {
    val expectedInOrderTraversal: Seq[Int] = Seq(6, 10, 2, 5, 3)
    val rewriter = new TopDownRewriter[IntTree] { override val rule = f }
    val actual = rewriter.rewriteTree(tree).preOrderMap(_.value)
    assert(actual == expectedInOrderTraversal)
  }

  test("bottomUpRewriter") {
    val expectedInOrderTraversal: Seq[Int] = Seq(12, 8, 2, 5, 3)
    val rewriter = new BottomUpRewriter[IntTree] { override val rule = f }
    val actual = rewriter.rewriteTree(tree).preOrderMap(_.value)
    assert(actual == expectedInOrderTraversal)
  }
}
