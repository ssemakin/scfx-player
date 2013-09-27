package org.scfxplayer

import scalafx.scene.control._
import scalafx.scene.layout.Priority
import scalafx.collections.ObservableBuffer
import scalafx.scene.input.{DragEvent, MouseEvent}
import javafx.scene.input.{ClipboardContent, TransferMode}
import javafx.scene.control.CheckMenuItem
import javafx.event.EventHandler
import javafx.scene.{ control => jfxsc }




//http://blog.ngopal.com.np/2012/05/06/javafx-drag-and-drop-cell-in-listview/


class PlayListWidget(val musicRecItems:ObservableBuffer[MusicRecordItem]) {
  import scalafx.Includes._

  def attachDnDToColumn(column:TableColumn[MusicRecordItem, String]):TableColumn[MusicRecordItem, String] = {
    val oldFactory = column.delegate.cellFactoryProperty().getValue()
    column.cellFactory = f => {
      val res:TableCell[MusicRecordItem,String] = oldFactory.call(f)
      res.onDragDropped = (event:DragEvent) => {
        val db = event.getDragboard();
        val success = if (event.getDragboard().hasString()) {
          val text = db.getString();
          val idx = res.tableRow.value.indexProperty().getValue
          val movedO = musicRecItems.find(_.fullPath == text)
          val targetO = Option(musicRecItems.get(idx))
          movedO.zip(targetO).foreach{ case (moved,target) =>
            musicRecItems.remove(moved)
            val items = musicRecItems.toList
            val before = items.takeWhile(_ != target)
            val after = items.dropWhile(_ != target)
            musicRecItems.clear()
            musicRecItems.addAll((before ::: List(moved) ::: after): _*)
          }

          true;
        } else false
        event.setDropCompleted(success);
        event.consume();
      }
      res
    }
    column
  }

  val durationColumn = attachDnDToColumn(new TableColumn[MusicRecordItem, String]() {
    text = "Duration"
    prefWidth = 80
    minWidth = 50
    cellValueFactory = {_.value.duration}
  })




  val trackColumn = attachDnDToColumn(new TableColumn[MusicRecordItem, String] {
    text = "Track"
    prefWidth = 300
    minWidth = 50
    cellValueFactory = {_.value.trackNameMade}
  })


  val albumColumn = attachDnDToColumn(new TableColumn[MusicRecordItem, String]() {
    text = "Album"
    prefWidth = 120
    minWidth = 50
    cellValueFactory = {_.value.album}
  })


  val artistColumn = attachDnDToColumn(new TableColumn[MusicRecordItem, String]() {
    text = "Artist"
    prefWidth = 120
    minWidth = 50
    cellValueFactory = {_.value.artist}
  })


  val durationMenuItem = new CheckMenuItem("Duration")
  initPlaylistMenuItem(durationMenuItem, durationColumn)
  val trackMenuItem = new CheckMenuItem("Track")
  initPlaylistMenuItem(trackMenuItem, trackColumn)
  val albumMenuItem = new CheckMenuItem("Album")
  initPlaylistMenuItem(albumMenuItem, albumColumn)
  val artistMenuItem = new CheckMenuItem("Artist")
  initPlaylistMenuItem(artistMenuItem, artistColumn)

  val playListSettingsMnu:ContextMenu = new ContextMenu {
    style = "-fx-background-radius: 10 0 10 10; -fx-border-color: white; -fx-border-radius: 10 0 10 10;"
    autoHide  = true
    items ++= List(durationMenuItem, trackMenuItem, albumMenuItem, artistMenuItem)
  }

  def initPlaylistMenuItem(i:CheckMenuItem, c:TableColumn[_,_]):Unit = {
    i.setSelected(true)
    i.setOnAction(
      new EventHandler[javafx.event.ActionEvent] {
        override def handle(event:javafx.event.ActionEvent) {
          event.consume()
          c.setVisible(i.isSelected)
        }
      })
  }



  //https://forums.oracle.com/thread/2413845
  def setupDragAndDrop(tableView:TableView[MusicRecordItem]):TableView[MusicRecordItem] = {
    tableView.onMouseClicked = (event:MouseEvent) => {
      if(event.getClickCount()==2){ // double click
        val selected = tableView.getSelectionModel().getSelectedItem();
        if(selected !=null){
          System.out.println("select : "+selected)
        }
      }
    }


    tableView.onDragDetected =  (event:MouseEvent) => {
        // drag was detected, start drag-and-drop gesture
        val selected = tableView.getSelectionModel().getSelectedItem();
        if(selected !=null){
          val db = tableView.startDragAndDrop(TransferMode.LINK)
          val content = new ClipboardContent();
          content.putString(selected.fullPath);
          db.setContent(content);
          event.consume();
     }
  }


    tableView.onDragOver =  (event:DragEvent) => {
        // data is dragged over the target
        val db = event.getDragboard();
        if (event.getDragboard().hasString()){
          event.acceptTransferModes(TransferMode.LINK);
        }
        event.consume();
    }


    tableView.onDragDropped = (event:DragEvent) => {
        val db = event.getDragboard();
        val success = if (event.getDragboard().hasString()) {
          println(event.gestureTarget)
          val text = db.getString();
          println(text)
          //musicRecItems.add(text);
          //tableView.setItems(tableContent);
          true;
        } else false
        event.setDropCompleted(success);
        event.consume();
      }
    tableView
  }


  def tableView():TableView[MusicRecordItem] = {
    val table = new TableView[MusicRecordItem](musicRecItems) {
      vgrow = Priority.ALWAYS
      hgrow = Priority.ALWAYS
      columns ++= List(durationColumn, trackColumn, albumColumn, artistColumn)
    }
    table.selectionModel.value.setSelectionMode(SelectionMode.MULTIPLE)




    setupDragAndDrop(table)
  }
}
