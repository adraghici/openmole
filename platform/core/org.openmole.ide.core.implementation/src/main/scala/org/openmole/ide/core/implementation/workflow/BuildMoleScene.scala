/*
 * Copyright (C) 2011 <mathieu.leclaire at openmole.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openmole.ide.core.implementation.workflow


import java.awt.Point
import org.netbeans.api.visual.action.ActionFactory
import org.netbeans.api.visual.action.ConnectProvider
import org.netbeans.api.visual.action.ReconnectProvider
import org.netbeans.api.visual.anchor.PointShape
import org.netbeans.api.visual.widget.ConnectionWidget
import org.netbeans.api.visual.widget.Scene
import org.netbeans.api.visual.widget.Widget
import org.openmole.ide.core.implementation.provider.DnDNewTaskProvider
import org.openmole.ide.core.implementation.provider.MoleSceneMenuProvider
import org.netbeans.api.visual.action.ConnectorState
import org.openmole.ide.core.implementation.provider.TransitionMenuProvider
import org.openmole.ide.core.model.commons.CapsuleType._
import org.openmole.ide.core.model.commons.TransitionType._
import org.openmole.ide.core.model.workflow.ICapsuleUI

class BuildMoleScene extends MoleScene{
  getActions.addAction(ActionFactory.createPopupMenuAction(new MoleSceneMenuProvider(this)))
  getActions.addAction(ActionFactory.createAcceptAction(new DnDNewTaskProvider(this)))
  
  val connectAction = ActionFactory.createExtendedConnectAction(connectLayer, new MoleSceneConnectProvider)
  val reconnectAction = ActionFactory.createReconnectAction(new MoleSceneReconnectProvider)
    
  override def initCapsuleAdd(w: ICapsuleUI)= obUI= Some(w.asInstanceOf[Widget])
  
  override def attachNodeWidget(n: String)= {
    capsuleLayer.addChild(obUI.get)
    obUI.get.createActions(CONNECT).addAction(connectAction)
    obUI.get.createActions(CONNECT).addAction(moveAction)
    obUI.get.getActions.addAction(createObjectHoverAction)
    obUI.get
  } 

  override def attachEdgeWidget(e: String)= {
    val connectionWidget = new LabeledConnectionWidget(this,manager.getTransition(e))
    connectLayer.addChild(connectionWidget);
    connectionWidget.setEndPointShape(PointShape.SQUARE_FILLED_BIG)
    connectionWidget.getActions.addAction(createObjectHoverAction)
    connectionWidget.getActions.addAction(createSelectAction)
    connectionWidget.getActions.addAction(reconnectAction)
    // connectionWidget.getActions.addAction(new TransitionActions(manager.getTransition(e),connectionWidget))
    connectionWidget.getActions.addAction(ActionFactory.createPopupMenuAction(new TransitionMenuProvider(this,connectionWidget)));
    connectionWidget
  }

  override def attachEdgeSourceAnchor(edge: String, oldSourceNode: String,sourceNode: String)= {
    val cw = findWidget(edge).asInstanceOf[LabeledConnectionWidget]
    cw.setSourceAnchor(new OutputSlotAnchor(findWidget(sourceNode).asInstanceOf[CapsuleUI]))
  }
  
  override def attachEdgeTargetAnchor(edge: String,oldTargetNode: String,targetNode: String) = findWidget(edge).asInstanceOf[LabeledConnectionWidget].setTargetAnchor(new InputSlotAnchor((findWidget(targetNode).asInstanceOf[CapsuleUI]), currentSlotIndex))
    
  
  class MoleSceneConnectProvider extends ConnectProvider {
    var source: Option[String]= None
    var target: Option[String]= None
    
    override def isSourceWidget(sourceWidget: Widget): Boolean= {
      val o= findObject(sourceWidget)
      source= None
      if (isNode(o)) source= Some(o.asInstanceOf[String])        
      var res= false
      sourceWidget match {
        case x: CapsuleUI=> {res = source.isDefined}
      }
      res
    }
    
    override def isTargetWidget(sourceWidget: Widget, targetWidget: Widget): ConnectorState = {
      val o= findObject(targetWidget)
      
      target= None
      if(isNode(o)) target= Some(o.asInstanceOf[String])
      if (targetWidget.getClass.equals(classOf[InputSlotWidget])){
        val iw= targetWidget.asInstanceOf[InputSlotWidget]
        if (! iw.startingSlot){
          currentSlotIndex = iw.index
          if (source.equals(target)) return ConnectorState.REJECT_AND_STOP
          else return ConnectorState.ACCEPT
        }
      }
      if (o == null) return ConnectorState.REJECT
      return ConnectorState.REJECT_AND_STOP
    }

    override def hasCustomTargetWidgetResolver(scene: Scene): Boolean= false
    
    override def resolveTargetWidget(scene: Scene, sceneLocation: Point): Widget= null
  
    override def createConnection(sourceWidget: Widget, targetWidget: Widget)= {
      val sourceCapsuleUI = sourceWidget.asInstanceOf[CapsuleUI]
      if (manager.registerTransition(sourceCapsuleUI, targetWidget.asInstanceOf[InputSlotWidget],if(sourceCapsuleUI.capsuleType == EXPLORATION_TASK) EXPLORATION_TRANSITION else BASIC_TRANSITION,None))
        createEdge(source.get, target.get)
    }
  }
  
  class MoleSceneReconnectProvider extends ReconnectProvider {

    var edge: Option[String]= None
    var originalNode: Option[String]= None
    var replacementNode: Option[String]= None
    
    override def reconnectingStarted(connectionWidget: ConnectionWidget,reconnectingSource: Boolean)= {}
    
    override def reconnectingFinished(connectionWidget: ConnectionWidget,reconnectingSource: Boolean)= {}
    
    def findConnection(connectionWidget: ConnectionWidget)= {
      val o= findObject(connectionWidget)
      edge= None
      if (isEdge(o)) edge= Some(o.asInstanceOf[String])
      originalNode= edge
    }
    
    override def isSourceReconnectable(connectionWidget: ConnectionWidget): Boolean = {
      val o= findObject(connectionWidget)
      edge= None
      if (isEdge(o)) edge= Some(o.asInstanceOf[String])
      originalNode = None
      if (edge.isDefined) originalNode= Some(getEdgeSource(edge.get))
      originalNode.isDefined
    }
    
    override def isTargetReconnectable(connectionWidget: ConnectionWidget): Boolean = {
      val o = findObject(connectionWidget)
      edge = None
      if (isEdge(o)) edge = Some(o.asInstanceOf[String])
      originalNode = None
      if (edge.isDefined) originalNode= Some(getEdgeTarget(edge.get))
      originalNode.isDefined
      
    }
    
    override def isReplacementWidget(connectionWidget: ConnectionWidget, replacementWidget: Widget, reconnectingSource: Boolean): ConnectorState = {
      val o= findObject(replacementWidget)
      replacementNode= None
      if (isNode(o)) replacementNode = Some(o.asInstanceOf[String])
      replacementWidget match {
        case x: OutputSlotWidget=> return ConnectorState.ACCEPT
        case x: InputSlotWidget=> {
            val iw= replacementWidget.asInstanceOf[InputSlotWidget]
            currentSlotIndex = iw.index
            return ConnectorState.ACCEPT
          }
        case _=> return ConnectorState.REJECT_AND_STOP
      }
    }
    
    override def hasCustomReplacementWidgetResolver(scene: Scene)= false
    
    override def resolveReplacementWidget(scene: Scene,sceneLocation: Point)= null
    
    override def reconnect(connectionWidget: ConnectionWidget,replacementWidget: Widget,reconnectingSource: Boolean)= {
      val t= manager.getTransition(edge.get)
      manager.removeTransition(edge.get)
      if (replacementWidget == null) removeEdge(edge.get)
      else if (reconnectingSource) {   
        setEdgeSource(edge.get, replacementNode.get)
        val sourceW = replacementWidget.asInstanceOf[OutputSlotWidget].capsule
        manager.registerTransition(edge.get,sourceW, t.target, if (sourceW.capsuleType == EXPLORATION_TASK) EXPLORATION_TRANSITION else BASIC_TRANSITION,None)
      }
      else {
        println("reconnect else ")
        val targetView= replacementWidget.asInstanceOf[InputSlotWidget]
        connectionWidget.setTargetAnchor(new InputSlotAnchor(targetView.capsule, currentSlotIndex))
        setEdgeTarget(edge.get, replacementNode.get)   
        manager.registerTransition(edge.get,t.source, targetView,if (targetView.capsule.capsuleType == EXPLORATION_TASK) EXPLORATION_TRANSITION else BASIC_TRANSITION,None)
      }
      repaint
    }
  }
}
