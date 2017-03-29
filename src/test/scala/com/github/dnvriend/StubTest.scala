/*
 * Copyright 2017 Dennis Vriend
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dnvriend

import com.github.dnvriend.StubTest._
import org.scalamock.scalatest.MockFactory

import scala.concurrent.Future

object StubTest {
  case class Person(id: String, name: String, age: Int)
  object Person {
    val Empty = Person("", "", 0)
  }
  trait PersonServiceClient {
    def getPersonById(id: String): Future[Option[Person]]
  }
}

class StubTest extends TestSpec with MockFactory {
  /**
   * With ScalaMock you can create objects that pretend to implement some trait or interface; 'the stub'
   * Then you can instruct that "faked" object how it should respond to all interactions with it.
   */
  def withService(f: PersonServiceClient => Unit): Unit = {
    val client = stub[PersonServiceClient]

    // configure behavior
    client.getPersonById _ when "222" returns Future.successful(Option(Person.Empty))
    client.getPersonById _ when "333" returns Future.successful(None)
    client.getPersonById _ when "-1" returns Future.failed(new RuntimeException("mocked"))
    f(client)
  }

  it should "find a person" in withService { service =>
    service.getPersonById("222").futureValue.value shouldBe Person.Empty
    service.getPersonById("333").futureValue should not be 'defined
    service.getPersonById("-1").toTry should be a 'failure
  }
}
