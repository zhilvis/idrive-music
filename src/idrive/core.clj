(ns idrive.core
  (require [clojure.java.io :as io]
           [clojure.string :as st])
  (import [java.nio.file OpenOption FileSystems]
          [java.nio.channels FileChannel]
          [java.nio ByteBuffer]))

(def files {"m4p" "BR3" "m4a" "BR3" "mp3" "BR4"})

(defn list-files [loc]
  (map #(io/file loc %) (.list (io/file loc))))

(defn dir? [f]
  (.isDirectory f))

(defn music? [f]
  (some true?
        (map #(.endsWith (.getName f) %) (keys files))))

(defn get-name [f]
  (.getName (io/file f)))

(defn find-music [loc]
  (remove nil?
          (flatten
            (for [f (list-files loc)]
              (if (dir? f)
                (find-music f)
                (if (music? f)
                  f))))))

(defn split [l]
  (st/split (.getPath (io/file l)) #"/"))

(defn base [l1 l2]
  (st/join "/"
           (drop-while
             #(contains? (set (split l2)) %) (split l1))))

(defn get-out-file [f l from]
  (let [path (st/split (base f from) #"\.")
        ext (files (last path))]
    (io/file l (str (apply str (butlast path)) "." ext))))

(defn negate-file [inf outf]
  (println inf " --> " outf)
  (.mkdirs (.getParentFile (io/file outf)))
  (FileNegator/negate inf outf))

(defn- get-folder-names [loc]
  (map #(.getName %) (filter dir? (list-files loc))))

(defn data-line [d1 d2 size]
  (str "/" d1 "/" "\t" d2 "\t" size "\t" 2 ))

(defn count-size [loc]
  (reduce + (map #(.length %) (list-files loc))))

(defn rename-dir [dir new-name]
  (let [d (io/file dir)]
    (.renameTo (io/file d) (io/file (.getParentFile d) new-name))))

(defn prepare-data-file [loc]
  (st/join "\t\n"
           (let [dirs (filter dir? (list-files loc))
                 index (range 1 (inc (count dirs)))]
             (for [[i dir] (into (sorted-map) (zipmap index dirs))]
               (let [new-dir (str "Ripped" i)
                     size (count-size dir)]
                 (rename-dir dir new-dir)
                 (data-line new-dir (.getName dir) size))))))

(defn do-decode [from to]
  (let [tof (io/file to "BMWData/Music")]
    (doall
      (map #(negate-file % (get-out-file % tof from))
           (find-music from)))
    (spit (io/file to "BMWData/BMWBackup.ver") "V1\n")
    (spit (io/file tof "data_1") (str (prepare-data-file tof) "\t\n"))))

(defn -main [from to]
  (do-decode from to))
