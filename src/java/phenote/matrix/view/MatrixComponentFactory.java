package phenote.matrix.view;

import org.bbop.framework.AbstractComponentFactory;
import phenote.matrix.model.MatrixController;

public class MatrixComponentFactory extends AbstractComponentFactory<MatrixComponent> {

	private final MatrixController controller;
	
	public MatrixComponentFactory(MatrixController controller) {
		this.controller = controller;
	}
	
	@Override
	public MatrixComponent doCreateComponent(String id) {
		return new MatrixComponent(id, this.controller);
	}

	@Override
	public FactoryCategory getCategory() {
		return FactoryCategory.ANNOTATION;
	}

	@Override
	public String getID() {
		return "phenoscape_matrix_viewer";
	}

	@Override
	public String getName() {
		return "Matrix Viewer";
	}

	// Do I need to implement a isSingleton method like Jim does in his factory classes?
}
