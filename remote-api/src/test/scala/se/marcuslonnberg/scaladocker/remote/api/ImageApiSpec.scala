package se.marcuslonnberg.scaladocker.remote.api

import akka.actor.ActorSystem
import akka.stream.MaterializerSettings
import akka.stream.scaladsl2.FlowMaterializer
import akka.testkit.TestKit
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import se.marcuslonnberg.scaladocker.remote.models.ImageName

class ImageApiSpec extends TestKit(ActorSystem("image-api")) with FlatSpecLike with Matchers with ScalaFutures with BeforeAndAfterAll with IntegrationPatience {
  implicit val mat = FlowMaterializer(MaterializerSettings(system))

  val client = DockerClient()

  val imageName = ImageName("scala-docker/image:start")
  val imageNameNewTag = ImageName("scala-docker/image:new-tag")
  val imageNameDelete = ImageName("scala-docker/image:delete")

  override def beforeAll() = {
    val busybox = ImageName("busybox")
    // TODO: client.images.create(busybox)

    assume {
      client.images.tag(busybox, imageName).futureValue &&
        client.images.tag(imageName, imageNameDelete).futureValue
    }
  }

  override def afterAll() = {
    client.images.delete(imageNameNewTag).futureValue
  }

  "Image API" should "list images" in {
    currentImageNames() should contain(imageName)
  }

  it should "create (pull) an image" in {
    pending
    client.images.create(ImageName("busybox"))
  }

  it should "tag an image" in {
    currentImageNames() should contain(imageName)

    client.images.tag(imageName, imageNameNewTag).futureValue shouldBe true

    currentImageNames() should contain allOf(imageName, imageNameNewTag)
  }

  it should "delete an image" in {
    currentImageNames() should contain(imageNameDelete)

    client.images.delete(imageNameDelete).futureValue shouldBe true

    currentImageNames() should not contain imageNameDelete
  }

  def currentImageNames(): Seq[ImageName] = {
    val images = client.images.list.futureValue
    val names = images.flatMap(_.repoTags)
    names
  }
}