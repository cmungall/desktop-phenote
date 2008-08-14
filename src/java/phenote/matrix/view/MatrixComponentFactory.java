package phenote.matrix.view;

import org.bbop.framework.AbstractComponentFactory;
import phenote.matrix.model.MatrixController;

public class MatrixComponentFactory extends AbstractComponentFactory<MatrixComponent> {

	private final MatrixController controller;
	
	public MatrixComponentFactory(MatrixController controller) {
		this.controller = controller;
	}
	
	public MatrixComponent doCreateComponent(String id) {
		return new MatrixComponent(id, this.controller);
	}

	public FactoryCategory getCategory() {
		return FactoryCategory.ANNOTATION;
	}

	public String getID() {
		return "phenoscape_matrix_viewer";
	}

	public String getName() {
		return "Matrix Viewer";
	}
}
