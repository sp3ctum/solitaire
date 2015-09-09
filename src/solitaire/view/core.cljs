(ns ^:figwheel-always solitaire.view.core
    (:require [reagent.core :as reagent :refer [atom]]
              [figwheel.client :as fw]
              [solitaire.core.logic :as l]
              [solitaire.view.actions :as a]))

(enable-console-print!)

(defonce app-state (atom (l/new-game-state)))

(defn selectable [card-place-name]
  ;; stock can never be selected
  (let [properties
        { :class (when (= (:selected-place @app-state)
                          card-place-name)
                   "selected")}]
    (if-not (= :stock card-place-name)
      (merge properties {:on-click
                         #(do (a/select-or-move! app-state card-place-name)
                              (.stopPropagation %))})
      properties)))

(defn card
  [card-map card-place-name]
  (if-not (:facing-up card-map)
    [:div.card-size.facing-down
     {:on-click #(a/turn-card! app-state card-map card-place-name)}]

    [:div.card-size.card-face {:key (:id card-map)}
     [:div.selected-overlay
      (selectable card-place-name)
      (when (:facing-up card-map)
        [:p.card-content
         {:class (if (#{:diamond :heart} (:suite card-map))
                   "red"
                   "black")}
         (:id card-map)])]]))

(defn card-place [app-state card-place-name & {:keys [fanned?]
                                               :or {fanned? false}}]
  (let [cards (get @app-state card-place-name)]
    (if (empty? cards)
      [:div.card-place.card-size
       (merge (selectable card-place-name)
              (when (= :stock card-place-name)
                {:on-click #(a/reset-stock! app-state)}))]
      [:div.card-place
       [:div.selected-overlay (selectable card-place-name)
        (if fanned?
          ;; the first card is normal,
          ;; the rest are overlapping
          [:div [card (first cards) card-place-name]
           [:div.overlapping-cards
            (doall (for [c (rest cards)]
                     ^{:key (:id c)}
                     [card c card-place-name]))]]
          [card (last cards) card-place-name])]])))

(defn board []
  [:div
   [:h1 "Klondike solitaire"]
   [:div.board.container-fluid
    {:on-click #(a/deselect! app-state)}
    [:div.row
     ;; top row
     [:div.col-xs-4
      [:div.col-xs-6 [card-place app-state :stock]]
      [:div.col-xs-6 [card-place app-state :waste-heap]]]
     [:div.col-xs-offset-1.col-xs-7.pull-right
      [:div.col-xs-3 [card-place app-state :foundation1]]
      [:div.col-xs-3 [card-place app-state :foundation2]]
      [:div.col-xs-3 [card-place app-state :foundation3]]
      [:div.col-xs-3 [card-place app-state :foundation4]]]]

    [:div.row.half-card-size]

    ;; bottom row
    [:div.row.card-size
     [:div.col-xs-2 [card-place app-state :tableau1 :fanned? true]]
     [:div.col-xs-2 [card-place app-state :tableau2 :fanned? true]]
     [:div.col-xs-2 [card-place app-state :tableau3 :fanned? true]]
     [:div.col-xs-2 [card-place app-state :tableau4 :fanned? true]]
     [:div.col-xs-2 [card-place app-state :tableau5 :fanned? true]]
     [:div.col-xs-2 [card-place app-state :tableau6 :fanned? true]]]
    ;; spacing
    [:div.row.card-size]]
   [:div.container
    [:div.row
     [:div.col-xs-2
      [:button.btn.btn-lg {:type "button"
                           :on-click #(a/new-game! app-state)}
       "New game"]]
     [:div.col-xs-2 [:button.btn.btn-lg {:type "button"
                                         :on-click #(a/undo! app-state)}
                     "Undo"]]]
    [:div.row [:div.pull-left
               [:h3 [:a {:href "test.html"} "Tests"]]]]]])

(fw/start {:build-id "dev"
           ;;:on-jsload #(print "loaded")
           })

(reagent/render-component [board]
                          (js/document.getElementById "app"))

