import org.apache.poi.sl.extractor.SlideShowExtractor
import org.apache.poi.sl.usermodel.SlideShowFactory
import org.apache.poi.xslf.usermodel.XMLSlideShow

class TextExtractor {

    def acceptedExtensions = ['ppt', 'pptx']

    private def newLine = '\n'

    String ppt(String path) {
        def slideShow = SlideShowFactory.create(new FileInputStream(path))
        def extractor = new SlideShowExtractor(slideShow)

        def results = []
        for (slide in slideShow.slides) {
            def text = extractor.getText(slide)

            def slideResult
            if (!(slide.notes == null)) {
                def notes = slide.notes.textParagraphs.findAll {
                    !it.isEmpty() && !(it.size() == 1 && (it.head().toString() == '' || it.head().toString() == '*'))
                }.flatten().join(newLine)

                slideResult = text + newLine + notes
            } else {
                slideResult = text
            }
            results.add(slideResult)
        }
        
        results.join(newLine)
    }

    String pptx(String path) {
        def slideShow = new XMLSlideShow(new FileInputStream(path))
        def extractor = new SlideShowExtractor(slideShow)
        
        def results = []
        for (slide in slideShow.slides) {
            def text = extractor.getText(slide)

            def slideResult
            if (!(slide.notes == null)) {
                def paragraphList = slide.notes.textParagraphs
                paragraphList.remove(paragraphList.size()-1)
                def notes = paragraphList.findAll {
                    !it.isEmpty() && !(it.size() == 1 && it.head().toString() == '')
                }.collect {
                    it.text
                }.flatten().join(newLine)

                slideResult = text + newLine + notes
            } else {
                slideResult = text
            }
            
            results.add(slideResult)
        }
        
        results.join(newLine)
    }

}
