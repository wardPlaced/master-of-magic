/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rafkind.masterofmagic.ui.framework

import org.newdawn.slick._;
import com.rafkind.masterofmagic.util._;

case class ComponentProperty(val name:String, val default:Any)
case class ComponentEventDescriptor(val name:String)

object Component {
  val LEFT = ComponentProperty("left", 0);
  val TOP = ComponentProperty("top", 0);
  val WIDTH = ComponentProperty("width", 0);
  val HEIGHT = ComponentProperty("height", 0);
  val BACKGROUND_IMAGE = ComponentProperty("background_image", null);

  val PROPERTY_CHANGED = ComponentEventDescriptor("property_changed");
}

case class ComponentEvent(val component:Component[_])

case class PropertyChangedEvent(override val component:Component[_],
                           val whatChanged:Tuple2[ComponentProperty, Any]) extends ComponentEvent(component:Component[_])

trait Component[T] {
  var properties = new scala.collection.mutable.HashMap[ComponentProperty, Any]();

  def set(settings:Tuple2[ComponentProperty, Any]*):T = {
    settings.foreach( (x:Tuple2[ComponentProperty, Any]) => {
        properties += x;
        
        listeners
          .get(Component.PROPERTY_CHANGED)
          .map(y =>
              y.foreach( 
                z =>
                  z(new PropertyChangedEvent(this, x))
                )
              );        
      }
    );
    this.asInstanceOf[T]
  }

  def getInt(key:ComponentProperty) = 
    properties.getOrElse(key, key.default).asInstanceOf[Int];

  def getImage(key:ComponentProperty) =
    properties.getOrElse(key, key.default).asInstanceOf[Image];

  var listeners = new CustomMultiMap[ComponentEventDescriptor, ComponentEvent => Unit];
  

  def listen(toWhat:ComponentEventDescriptor, andThen:(ComponentEvent => Unit)):T = {
    listeners.put(toWhat, andThen);
    this.asInstanceOf[T]
  }  
  
  def render(graphics:Graphics):T;
}